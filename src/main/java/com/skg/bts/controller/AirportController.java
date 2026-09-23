package com.skg.bts.controller;

import com.skg.bts.dto.AirportRequest;
import com.skg.bts.dto.AirportResponse;
import com.skg.bts.service.AirportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
@RequiredArgsConstructor
public class AirportController {

    private final AirportService airportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AirportResponse create(@Valid @RequestBody AirportRequest req) {
        return airportService.create(req);
    }

    @GetMapping
    public List<AirportResponse> findAll() {
        return airportService.findAll();
    }
}
