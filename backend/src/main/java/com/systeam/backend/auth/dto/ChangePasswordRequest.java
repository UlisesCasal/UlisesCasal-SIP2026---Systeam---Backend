package com.systeam.backend.auth.dto;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;


@Data
public class ChangePasswordRequest{
    @NotBlank
    private String currentPassword;

    @NotBlank
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$", message = "La contraseña debe contener al menos un número, una letra mayúscula, una minúscula y un carácter especial (@#$%^&+=!)")
    private String newPassword;
}