package com.skg.bts.dto;

import com.skg.bts.domain.enums.FlightStatus;

import java.time.Instant;

public record FlightResponse(
        Long id, String flightNo, String origin, String destination,
        Instant depTime, Instant arrTime, FlightStatus status
) {}
