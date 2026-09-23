package com.skg.bts.dto;

import com.skg.bts.domain.enums.BaggageStatus;

import java.time.Instant;

public record CheckpointHistoryItem(
        BaggageStatus checkpointType, String location, Instant timestamp, String notes
) {}
