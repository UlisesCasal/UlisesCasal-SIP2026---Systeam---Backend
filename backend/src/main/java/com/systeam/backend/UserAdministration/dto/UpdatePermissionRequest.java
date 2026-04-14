package com.systeam.backend.UserAdministration.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePermissionRequest {
    @NotBlank(message = "El nombre del permiso es obligatorio")
    private String name;
    private String description;
}
