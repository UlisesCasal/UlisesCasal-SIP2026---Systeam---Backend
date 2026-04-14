package com.systeam.backend.UserAdministration.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionResponse {
    private Long id;
    private String name;
    private String description;
}
