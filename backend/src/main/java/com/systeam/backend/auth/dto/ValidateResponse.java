package com.systeam.backend.auth.dto;
import java.util.Set;

public record ValidateResponse(
    Long userId,
    String email,
    Set<String> roles,
    Set<String> permissions
) {}
