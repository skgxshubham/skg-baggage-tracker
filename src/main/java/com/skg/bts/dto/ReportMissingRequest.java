package com.skg.bts.dto;

import jakarta.validation.constraints.NotNull;

// requesterUserId is temporary, until JWT gives us the authenticated principal
public record ReportMissingRequest(@NotNull Long requesterUserId) {}
