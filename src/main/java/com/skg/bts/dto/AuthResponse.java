package com.skg.bts.dto;

public record AuthResponse(String token, Long userId, String name, String role) {}
