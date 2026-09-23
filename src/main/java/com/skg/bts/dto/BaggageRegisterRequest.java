package com.skg.bts.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BaggageRegisterRequest(
        @NotNull Long passengerId,
        @NotNull Long flightId,
        @NotBlank String tagNumber,
        @NotNull @DecimalMin("0.1") BigDecimal weightKg
) {}
