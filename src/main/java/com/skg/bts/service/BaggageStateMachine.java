package com.skg.bts.service;

import com.skg.bts.domain.enums.BaggageStatus;
import com.skg.bts.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class BaggageStateMachine {

    private static final Map<BaggageStatus, Set<BaggageStatus>> ALLOWED = new EnumMap<>(BaggageStatus.class);

    static {
        ALLOWED.put(BaggageStatus.CHECKED_IN, EnumSet.of(BaggageStatus.SECURITY_CLEARED, BaggageStatus.MISHANDLED));
        ALLOWED.put(BaggageStatus.SECURITY_CLEARED, EnumSet.of(BaggageStatus.LOADED, BaggageStatus.MISHANDLED));
        ALLOWED.put(BaggageStatus.LOADED, EnumSet.of(BaggageStatus.IN_TRANSIT, BaggageStatus.MISHANDLED));
        ALLOWED.put(BaggageStatus.IN_TRANSIT, EnumSet.of(BaggageStatus.ARRIVED, BaggageStatus.MISHANDLED));
        ALLOWED.put(BaggageStatus.ARRIVED, EnumSet.of(BaggageStatus.ON_CAROUSEL, BaggageStatus.MISHANDLED));
        ALLOWED.put(BaggageStatus.ON_CAROUSEL, EnumSet.noneOf(BaggageStatus.class)); // terminal
        ALLOWED.put(BaggageStatus.MISHANDLED, EnumSet.noneOf(BaggageStatus.class));  // terminal until resolved manually
    }

    public void assertValidTransition(BaggageStatus from, BaggageStatus to) {
        if (!ALLOWED.getOrDefault(from, Set.of()).contains(to)) {
            throw new InvalidStateTransitionException(
                    "Cannot transition baggage from %s to %s".formatted(from, to));
        }
    }
}
