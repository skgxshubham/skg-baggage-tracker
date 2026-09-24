package com.skg.bts.controller;

import com.skg.bts.dto.*;
import com.skg.bts.service.BaggageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.skg.bts.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/baggage")
@RequiredArgsConstructor
public class BaggageController {

    private final BaggageService baggageService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BaggageResponse register(@Valid @RequestBody BaggageRegisterRequest req) {
        return baggageService.register(req);
    }

    @PostMapping("/{id}/checkpoint")
    public BaggageResponse addCheckpoint(@PathVariable Long id, @Valid @RequestBody CheckpointRequest req) {
        return baggageService.addCheckpoint(id, req);
    }

    @PostMapping("/{id}/report-missing")
    public BaggageResponse reportMissing(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser principal) {
        return baggageService.reportMissing(id, principal.userId());
    }

    @GetMapping("/{tagNumber}/track")
    public PublicTrackingResponse trackPublic(@PathVariable String tagNumber) {
        return baggageService.trackPublic(tagNumber);
    }

    @GetMapping("/{tagNumber}/track/detail")
    public DetailTrackingResponse trackDetail(@PathVariable String tagNumber,
                                              @AuthenticationPrincipal AuthenticatedUser principal) {
        return baggageService.trackDetail(tagNumber, principal.userId(), principal.role());
    }
}
