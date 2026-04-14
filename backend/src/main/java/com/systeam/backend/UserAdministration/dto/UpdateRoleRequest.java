package com.systeam.backend.UserAdministration.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateRoleRequest {
    @NotBlank(message = "El nombre del rol es obligatorio")
    private String name;
    private String description;
}
