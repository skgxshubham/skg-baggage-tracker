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
        // TODO (Day 8): update the Redis cache here in the same flow.
        // TODO (Week 3-4): publish a baggage-events Kafka message here.

        return toResponse(baggageRepository.save(baggage));
    }

    public BaggageResponse reportMissing(Long baggageId) {
        Baggage baggage = getEntityById(baggageId);
        // MISHANDLED is reachable from any non-terminal state, so we bypass the normal
        // adjacency check here rather than special-casing it inside the state machine.
        baggage.setCurrentStatus(BaggageStatus.MISHANDLED);
        baggageRepository.save(baggage);

        checkpointRepository.save(BaggageCheckpoint.builder()
                .baggage(baggage)
                .checkpointType(BaggageStatus.MISHANDLED)
                .location(baggage.getCurrentLocation())
                .notes("Reported missing by passenger")
                .build());

        return toResponse(baggage);
    }

    @Transactional(readOnly = true)
    public PublicTrackingResponse trackPublic(String tagNumber) {
        Baggage baggage = getEntityByTag(tagNumber);
        return new PublicTrackingResponse(
                baggage.getCurrentStatus(),
                baggage.getCurrentLocation(),
                baggage.getUpdatedAt(),
                baggage.getFlight().getFlightNo()
        );
    }

    @Transactional(readOnly = true)
    public DetailTrackingResponse trackDetail(String tagNumber) {
        // NOTE: ownership/staff-admin authorization (section 2.3) is not enforced yet —
        // it goes in during the JWT phase, in the service layer per the design doc.
        Baggage baggage = getEntityByTag(tagNumber);
        Passenger passenger = baggage.getPassenger();

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
