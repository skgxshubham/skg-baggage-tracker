package com.skg.bts.service;

import com.skg.bts.domain.Airport;
import com.skg.bts.domain.Flight;
import com.skg.bts.dto.FlightRequest;
import com.skg.bts.dto.FlightResponse;
import com.skg.bts.exception.ResourceNotFoundException;
import com.skg.bts.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FlightService {

    private final FlightRepository flightRepository;
    private final AirportService airportService;

    public FlightResponse create(FlightRequest req) {
        Airport origin = airportService.getEntityById(req.originAirportId());
        Airport dest = airportService.getEntityById(req.destAirportId());

        Flight saved = flightRepository.save(Flight.builder()
                .flightNo(req.flightNo())
                .origin(origin)
                .destination(dest)
                .depTime(req.depTime())
                .arrTime(req.arrTime())
                .build());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FlightResponse> findAll() {
        return flightRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Flight getEntityById(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found: " + id));
    }

    private FlightResponse toResponse(Flight f) {
        return new FlightResponse(f.getId(), f.getFlightNo(), f.getOrigin().getCode(),
                f.getDestination().getCode(), f.getDepTime(), f.getArrTime(), f.getStatus());
    }
}
