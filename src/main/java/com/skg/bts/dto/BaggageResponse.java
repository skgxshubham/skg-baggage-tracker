package com.skg.bts.dto;

import com.skg.bts.domain.enums.BaggageStatus;

import java.math.BigDecimal;

public record BaggageResponse(
        Long id, String tagNumber, BigDecimal weightKg,
        BaggageStatus currentStatus, String currentLocation
) {}
