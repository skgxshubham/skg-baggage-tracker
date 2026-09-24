package com.skg.bts.security;

// Lightweight principal placed in the SecurityContext by the JWT filter.
// Avoids a DB hit per request just to know who's calling and what role they have.
public record AuthenticatedUser(Long userId, String email, String role) {}
