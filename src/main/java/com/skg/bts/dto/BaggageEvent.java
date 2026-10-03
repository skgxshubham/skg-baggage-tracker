package com.skg.bts.dto;

import com.skg.bts.domain.enums.BaggageStatus;

import java.time.Instant;

public record BaggageEvent(
        Long baggageId,
        String tagNumber,
        BaggageStatus checkpointType,
        String location,
        Instant timestamp,
        Long passengerUserId
) {}
