package com.systeam.backend.UserAdministration.dto;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponse {
    private Long id;
    private String name;
    private String description;
    private Set<String> permissions;
}
