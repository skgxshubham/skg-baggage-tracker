package com.skg.bts.controller;

import com.skg.bts.dto.FlightRequest;
import com.skg.bts.dto.FlightResponse;
import com.skg.bts.service.FlightService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FlightResponse create(@Valid @RequestBody FlightRequest req) {
        return flightService.create(req);
    }

    @GetMapping
    public List<FlightResponse> findAll() {
        return flightService.findAll();
    }
}
