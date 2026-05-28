package com.systeam.backend.auth.controller;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.systeam.backend.UserAdministration.dto.CreateUserRequest;
import com.systeam.backend.UserAdministration.dto.UserResponse;
import com.systeam.backend.UserAdministration.service.UserService;
import com.systeam.backend.auth.dto.ChangePasswordRequest;
import com.systeam.backend.auth.dto.LoginRequest;
import com.systeam.backend.auth.dto.LoginResponse;
import com.systeam.backend.auth.dto.ValidateResponse;
import com.systeam.backend.auth.security.JwtService;
import com.systeam.backend.auth.service.AuthService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.InvalidKeyException;
import jakarta.validation.Valid;

//ENDPOINT CON EL CUAL SE PUEDE AUTENTICAR Y REGISTRAR USUARIOS
//POR CADA PETICION QUE RECIBE LO REDIRIGE AL SERVICE DE AUTH.
// --------------------
// --------------------
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, UserService userService, JwtService jwtService) {
        this.authService = authService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) throws InvalidKeyException, Exception {
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

    @GetMapping("/validate")
    public ValidateResponse validate(@RequestHeader("Authorization") String authHeader) throws Exception {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtService.extractClaims(token, claims -> claims.get("userId", Long.class));
        String email = jwtService.extractClaims(token, Claims::getSubject);
        List<String> rolesList = jwtService.extractClaims(token, claims -> claims.get("roles", List.class));
        List<String> permissionsList = jwtService.extractClaims(token, claims -> claims.get("permissions", List.class));
        Set<String> roles = rolesList != null ? new HashSet<>(rolesList) : new HashSet<>();
        Set<String> permissions = permissionsList != null ? new HashSet<>(permissionsList) : new HashSet<>();
        return new ValidateResponse(userId, email, roles, permissions);
    }
}
