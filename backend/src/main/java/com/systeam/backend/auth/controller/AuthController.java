package com.systeam.backend.auth.controller;

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.systeam.backend.UserAdministration.dto.CreateUserRequest;
import com.systeam.backend.UserAdministration.dto.UserResponse;
import com.systeam.backend.UserAdministration.repository.UserRepository;
import com.systeam.backend.UserAdministration.service.UserService;
import com.systeam.backend.auth.dto.ChangePasswordRequest;
import com.systeam.backend.auth.dto.LoginRequest;
import com.systeam.backend.auth.dto.LoginResponse;
import com.systeam.backend.auth.dto.RefreshRequest;
import com.systeam.backend.auth.dto.TokenValidationResponse;
import com.systeam.backend.auth.service.AuthService;
import com.systeam.backend.auth.service.RefreshTokenService;

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
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserService userService,
                          RefreshTokenService refreshTokenService,
                          UserRepository userRepository) {
        this.authService = authService;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.userRepository = userRepository;
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

    // ENDPOINT PARA REFRESCAR TOKENS
    @PostMapping("/refresh")
    public LoginResponse refreshToken(@RequestBody @Valid RefreshRequest request) throws Exception {
        return refreshTokenService.refreshTokenPair(request.getRefreshToken());
    }

    // ENDPOINT PARA QUE OTROS MICROSERVICIOS VALIDEN UN TOKEN
    // Si el request llega acá, el JwtAuthenticationFilter ya validó la firma y expiración.
    // Solo retornamos los datos del usuario que están en el SecurityContext.
    @GetMapping("/validate")
    public TokenValidationResponse validateToken(Principal principal) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .collect(Collectors.toSet());

        Set<String> permissions = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> !a.startsWith("ROLE_"))
                .collect(Collectors.toSet());

        Long userId = userRepository.findByEmail(principal.getName())
                .map(u -> u.getId())
                .orElseThrow();

        return new TokenValidationResponse(userId, principal.getName(), roles, permissions);
    }
}
