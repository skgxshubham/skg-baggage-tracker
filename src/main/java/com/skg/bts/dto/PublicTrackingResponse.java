package com.skg.bts.dto;

import com.skg.bts.domain.enums.BaggageStatus;

import java.time.Instant;

public record PublicTrackingResponse(
        BaggageStatus status, String location, Instant lastUpdated, String flightNo
) {}
