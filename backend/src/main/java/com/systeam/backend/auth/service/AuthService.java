package com.systeam.backend.auth.service;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.systeam.backend.UserAdministration.dto.CreateUserRequest;
import com.systeam.backend.UserAdministration.dto.UserResponse;
import com.systeam.backend.UserAdministration.model.User;
import com.systeam.backend.UserAdministration.repository.UserRepository;
import com.systeam.backend.UserAdministration.service.UserService;
import com.systeam.backend.auth.dto.LoginRequest;
import com.systeam.backend.auth.dto.LoginResponse;
import com.systeam.backend.auth.security.CustomUserDetailsService;
import com.systeam.backend.auth.security.JwtService;

import lombok.RequiredArgsConstructor;
//SERVICIO DE AUTENTICACION, contiene los servicios para autenticar y registrar usuarios
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    //Detalles del usuario
    private final CustomUserDetailsService customUserDetailsService;
    //Servicio para validar tokenks JWT
    private final JwtService jwtService;
    //Acceso a la tbla de usuarios
    private final UserRepository userRepository;
    //Servicio para crear usuarios
    private final UserService userService;

    //Servicio para autenticar usuarios, recibe una request de login
    public LoginResponse login(LoginRequest request) {
        //Autenticar usuario con email y contraseña contra el Authenticator manager
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        //Obtengo los detalles del usuario
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getEmail());
        //Obtengo el usuario de la tbla de usuarios
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        //Obtengo los roles y permisos del usuario obtenidos de la tabla de roles y permisos de usuarios
        Set<String> roles = user.getRoles().stream()
            .map(role -> role.getName())
            .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getName())
            .collect(Collectors.toSet());
        // --------------------
        //Genero el token Jwt con los detalles del usuario y los roles y permisos
        String token = jwtService.generateToken(userDetails, Map.of(
            "userId", user.getId(),
            "roles", roles,
            "permissions", permissions
        ));
        // --------------------
        //Devuelvo la respuesta de login con el token y los detalles del usuario
        return LoginResponse.builder()
            .accessToken(token)
            .tokenType("Bearer")
            .expiresIn(jwtService.getExpirationMs())
            .userId(user.getId())
            .email(user.getEmail())
            .roles(roles)
            .permissions(permissions)
            .build();
    }
    //Servicio para registrar usuarios, recibe una request de registro
    // --------------------
    public UserResponse register(CreateUserRequest request) {
        return userService.createUser(request);
    }
}
