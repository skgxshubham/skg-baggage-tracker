package com.skg.bts.service;

import com.skg.bts.domain.enums.BaggageStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;

// Centralizes "how long should this status stay cached" so the actual
// durations are easy to find and tune without hunting through service code.
@Component
public class BaggageCacheTtlPolicy {

    private static final Duration SHORT_TTL = Duration.ofSeconds(30);      // ARRIVED, ON_CAROUSEL — bag's basically done
    private static final Duration LONG_TTL = Duration.ofMinutes(10);       // MISHANDLED — status won't change quickly
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(2);     // everything in progress

    public Duration ttlFor(BaggageStatus status) {
        return switch (status) {
            case ARRIVED, ON_CAROUSEL -> SHORT_TTL;
            case MISHANDLED -> LONG_TTL;
            default -> DEFAULT_TTL;
        };
    }
}