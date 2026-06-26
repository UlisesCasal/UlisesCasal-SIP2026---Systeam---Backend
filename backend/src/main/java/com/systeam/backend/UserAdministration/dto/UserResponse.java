package com.systeam.backend.UserAdministration.dto;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;
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
    private Set<String> permissions;
    private LocalDateTime createdAt;
    private LocalDate fechaNacimiento;
    private BigDecimal saldoIdea;
    private BigDecimal saldoUsdt;
    private LocalDateTime deletedAt;
    private String kycStatus;
    private String kycProviderId;
}