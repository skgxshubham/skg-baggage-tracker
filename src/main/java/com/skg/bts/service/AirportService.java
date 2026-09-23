package com.skg.bts.service;

import com.skg.bts.domain.Airport;
import com.skg.bts.dto.AirportRequest;
import com.skg.bts.dto.AirportResponse;
import com.skg.bts.exception.ResourceNotFoundException;
import com.skg.bts.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AirportService {

    private final AirportRepository airportRepository;

    public AirportResponse create(AirportRequest req) {
        if (airportRepository.existsByCode(req.code())) {
            throw new IllegalArgumentException("Airport code already exists: " + req.code());
        }
        Airport saved = airportRepository.save(Airport.builder()
                .code(req.code().toUpperCase())
                .name(req.name())
                .city(req.city())
                .build());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AirportResponse> findAll() {
        return airportRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Airport getEntityById(Long id) {
        return airportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airport not found: " + id));
    }

    private AirportResponse toResponse(Airport a) {
        return new AirportResponse(a.getId(), a.getCode(), a.getName(), a.getCity());
    }
}
