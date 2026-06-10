package com.systeam.backend.UserAdministration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import com.systeam.backend.UserAdministration.validation.ValidAge;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateUserRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    private Boolean enabled;

    @Past(message = "La fecha de nacimiento debe estar en el pasado")
    @ValidAge(min = 18, message = "Debe ser mayor de 18 años para ingresar a la plataforma")
    private LocalDate fechaNacimiento;
}