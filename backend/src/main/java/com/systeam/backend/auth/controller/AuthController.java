package com.systeam.backend.auth.controller;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.systeam.backend.UserAdministration.dto.CreateUserRequest;
import com.systeam.backend.UserAdministration.dto.UserResponse;
import com.systeam.backend.UserAdministration.service.UserService;
import com.systeam.backend.auth.dto.ChangePasswordRequest;
import com.systeam.backend.auth.dto.LoginRequest;
import com.systeam.backend.auth.dto.LoginResponse;
import com.systeam.backend.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

//ENDPOINT CON EL CUAL SE PUEDE AUTENTICAR Y REGISTRAR USUARIOS
//POR CADA PETICION QUE RECIBE LO REDIRIGE AL SERVICE DE AUTH.
// --------------------
// --------------------
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody @Valid CreateUserRequest request) {
        return authService.register(request);
    }
    //Es un ENDPOINT QUE REQUIERE AUTENTICACION PARA CAMBIAR CONTRASEÑA
    @PostMapping("/change-password")
    public void changePassword(
            Principal principal,
            @RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(
                principal.getName(),
                request.getCurrentPassword(),
                request.getNewPassword());
    }
}
