package com.skg.bts.dto;

import com.skg.bts.domain.enums.BaggageStatus;
import jakarta.validation.constraints.NotNull;

public record CheckpointRequest(
        @NotNull BaggageStatus checkpointType,
        String location,
        String notes
) {}
