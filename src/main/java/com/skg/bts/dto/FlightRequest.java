package com.skg.bts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record FlightRequest(
        @NotBlank String flightNo,
        @NotNull Long originAirportId,
        @NotNull Long destAirportId,
        @NotNull Instant depTime,
        @NotNull Instant arrTime
) {}
