package com.skg.bts.service;

import com.skg.bts.domain.*;
import com.skg.bts.domain.enums.BaggageStatus;
import com.skg.bts.dto.*;
import com.skg.bts.exception.ResourceNotFoundException;
import com.skg.bts.repository.BaggageCheckpointRepository;
import com.skg.bts.repository.BaggageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BaggageService {

    // How long a bag can sit at one checkpoint before the sweep flags it. Tune freely.
    private static final long STUCK_THRESHOLD_MINUTES = 120;

    private final BaggageRepository baggageRepository;
    private final BaggageCheckpointRepository checkpointRepository;
    private final BaggageStateMachine stateMachine;
    private final PassengerService passengerService;
    private final FlightService flightService;
    private final BaggageCacheService  cacheService;
    private final BaggageCacheTtlPolicy ttlPolicy;
    private final OutboxService outboxService;

    public BaggageResponse register(BaggageRegisterRequest req) {
        Passenger passenger = passengerService.getEntityById(req.passengerId());
        Flight flight = flightService.getEntityById(req.flightId());

        Baggage baggage = Baggage.builder()
                .passenger(passenger)
                .flight(flight)
                .tagNumber(req.tagNumber())
                .weightKg(req.weightKg())
                .currentStatus(BaggageStatus.CHECKED_IN)
                .currentLocation(flight.getOrigin().getCode())
                .build();
        baggage = baggageRepository.save(baggage);

        checkpointRepository.save(BaggageCheckpoint.builder()
                .baggage(baggage)
                .checkpointType(BaggageStatus.CHECKED_IN)
                .location(flight.getOrigin().getCode())
                .notes("Baggage registered at check-in")
                .build());

        // Write-through: populate the cache the moment the bag exists,
        // so the very first tracking read is already a cache hit.
        CachedBaggageStatus cached = new CachedBaggageStatus(
                baggage.getCurrentStatus(), baggage.getCurrentLocation(),
                baggage.getUpdatedAt(), baggage.getFlight().getFlightNo());
        cacheService.put(baggage.getTagNumber(), cached, ttlPolicy.ttlFor(baggage.getCurrentStatus()));

        BaggageEvent event = new BaggageEvent(
                baggage.getId(), baggage.getTagNumber(), baggage.getCurrentStatus(),
                baggage.getCurrentLocation(), baggage.getUpdatedAt(),
                baggage.getPassenger().getUser().getId());
        outboxService.enqueue(event);

        return toResponse(baggage);
    }

    public BaggageResponse addCheckpoint(Long baggageId, CheckpointRequest req) {
        Baggage baggage = getEntityById(baggageId);
        stateMachine.assertValidTransition(baggage.getCurrentStatus(), req.checkpointType());

        checkpointRepository.save(BaggageCheckpoint.builder()
                .baggage(baggage)
                .checkpointType(req.checkpointType())
                .location(req.location())
                .notes(req.notes())
                .build());

        baggage.setCurrentStatus(req.checkpointType());
        if (req.location() != null) {
            baggage.setCurrentLocation(req.location());
        }
        Baggage saved = baggageRepository.save(baggage);

        // Write-through: update the cache in the same flow as the DB write,
        // right after the DB commit succeeds.
        CachedBaggageStatus cached = new CachedBaggageStatus(
                saved.getCurrentStatus(), saved.getCurrentLocation(),
                saved.getUpdatedAt(), saved.getFlight().getFlightNo());
        cacheService.put(saved.getTagNumber(), cached, ttlPolicy.ttlFor(saved.getCurrentStatus()));

        BaggageEvent event = new BaggageEvent(
                saved.getId(), saved.getTagNumber(), saved.getCurrentStatus(),
                saved.getCurrentLocation(), saved.getUpdatedAt(),
                saved.getPassenger().getUser().getId());
        outboxService.enqueue(event);

        return toResponse(saved);
    }

    public BaggageResponse reportMissing(Long baggageId, Long requesterUserId) {
        Baggage baggage = getEntityById(baggageId);

        boolean isOwner = baggage.getPassenger().getUser().getId().equals(requesterUserId);
        if (!isOwner) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only the owning passenger can report this baggage missing");
        }

        baggage.setCurrentStatus(BaggageStatus.MISHANDLED);
        Baggage saved = baggageRepository.save(baggage);

        checkpointRepository.save(BaggageCheckpoint.builder()
                .baggage(saved)
                .checkpointType(BaggageStatus.MISHANDLED)
                .location(saved.getCurrentLocation())
                .notes("Reported missing by passenger")
                .build());

        // Write-through: MISHANDLED gets the long TTL from the policy.
        CachedBaggageStatus cached = new CachedBaggageStatus(
                saved.getCurrentStatus(), saved.getCurrentLocation(),
                saved.getUpdatedAt(), saved.getFlight().getFlightNo());
        cacheService.put(saved.getTagNumber(), cached, ttlPolicy.ttlFor(saved.getCurrentStatus()));

        BaggageEvent event = new BaggageEvent(
                saved.getId(), saved.getTagNumber(), saved.getCurrentStatus(),
                saved.getCurrentLocation(), saved.getUpdatedAt(),
                saved.getPassenger().getUser().getId());
        outboxService.enqueue(event);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PublicTrackingResponse trackPublic(String tagNumber) {
        CachedBaggageStatus cached = cacheService.get(tagNumber);
        if (cached != null) {
            return new PublicTrackingResponse(cached.status(), cached.location(), cached.lastUpdated(), cached.flightNo());
        }

        // Cache miss (cold cache, eviction, or Redis unavailable) — fall back to
        // Postgres and repopulate the cache so subsequent reads are fast again.
        Baggage baggage = getEntityByTag(tagNumber);
        CachedBaggageStatus fresh = new CachedBaggageStatus(
                baggage.getCurrentStatus(), baggage.getCurrentLocation(),
                baggage.getUpdatedAt(), baggage.getFlight().getFlightNo());
        cacheService.put(tagNumber, fresh, ttlPolicy.ttlFor(baggage.getCurrentStatus()));

        return new PublicTrackingResponse(fresh.status(), fresh.location(), fresh.lastUpdated(), fresh.flightNo());
    }

    @Transactional(readOnly = true)
    public DetailTrackingResponse trackDetail(String tagNumber, Long requesterUserId, String requesterRole) {
        Baggage baggage = getEntityByTag(tagNumber);
        Passenger passenger = baggage.getPassenger();

        boolean isOwner = passenger.getUser().getId().equals(requesterUserId);
        boolean isStaffOrAdmin = requesterRole.equals("STAFF") || requesterRole.equals("ADMIN");
        if (!isOwner && !isStaffOrAdmin) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not authorized to view this baggage's full detail");
        }

        List<CheckpointHistoryItem> history = checkpointRepository
                .findByBaggageIdOrderByTimestampAsc(baggage.getId())
                .stream()
                .map(c -> new CheckpointHistoryItem(c.getCheckpointType(), c.getLocation(), c.getTimestamp(), c.getNotes()))
                .toList();

        return new DetailTrackingResponse(
                baggage.getTagNumber(),
                baggage.getCurrentStatus(),
                baggage.getCurrentLocation(),
                baggage.getWeightKg(),
                passenger.getUser().getName(),
                passenger.getPnr(),
                passenger.getSeatNo(),
                history
        );
    }

    @Transactional(readOnly = true)
    public List<BaggageResponse> findMishandled() {
        return baggageRepository.findByCurrentStatus(BaggageStatus.MISHANDLED)
                .stream().map(this::toResponse).toList();
    }

    // Scheduled sweep from section 2.2: flags bags stuck at one checkpoint too long.
    // DB-only for now; the Kafka-based mishandled-detector consumer replaces/complements
    // this once messaging is wired up.
    @Scheduled(fixedDelayString = "PT5M")
    @Transactional
    public void sweepStuckBaggage() {
        Instant threshold = Instant.now().minus(STUCK_THRESHOLD_MINUTES, ChronoUnit.MINUTES);
        List<BaggageStatus> excluded = List.of(BaggageStatus.MISHANDLED, BaggageStatus.ON_CAROUSEL);

        List<Baggage> stuck = baggageRepository.findByCurrentStatusNotInAndUpdatedAtBefore(excluded, threshold);
        for (Baggage baggage : stuck) {
            baggage.setCurrentStatus(BaggageStatus.MISHANDLED);
            baggageRepository.save(baggage);

            checkpointRepository.save(BaggageCheckpoint.builder()
                    .baggage(baggage)
                    .checkpointType(BaggageStatus.MISHANDLED)
                    .location(baggage.getCurrentLocation())
                    .notes("Auto-flagged: stuck beyond threshold")
                    .build());
        }
    }

    private Baggage getEntityById(Long id) {
        return baggageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Baggage not found: " + id));
    }

    private Baggage getEntityByTag(String tagNumber) {
        return baggageRepository.findByTagNumber(tagNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Baggage not found: " + tagNumber));
    }

    private BaggageResponse toResponse(Baggage b) {
        return new BaggageResponse(b.getId(), b.getTagNumber(), b.getWeightKg(), b.getCurrentStatus(), b.getCurrentLocation());
    }
}
