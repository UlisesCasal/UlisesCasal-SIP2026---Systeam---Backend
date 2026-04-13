package com.systeam.backend.UserAdministration.dto;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;
}