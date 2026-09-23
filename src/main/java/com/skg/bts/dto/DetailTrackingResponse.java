package com.skg.bts.dto;

import com.skg.bts.domain.enums.BaggageStatus;

import java.math.BigDecimal;
import java.util.List;

public record DetailTrackingResponse(
        String tagNumber,
        BaggageStatus currentStatus,
        String currentLocation,
        BigDecimal weightKg,
        String passengerName,
        String pnr,
        String seatNo,
        List<CheckpointHistoryItem> checkpointHistory
) {}
