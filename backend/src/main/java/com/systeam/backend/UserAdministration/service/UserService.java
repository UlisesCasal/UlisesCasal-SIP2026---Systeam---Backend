package com.systeam.backend.UserAdministration.service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.systeam.backend.UserAdministration.dto.CreateUserRequest;
import com.systeam.backend.UserAdministration.dto.UpdateUserRequest;
import com.systeam.backend.UserAdministration.dto.UserResponse;
import com.systeam.backend.UserAdministration.model.Role;
import com.systeam.backend.UserAdministration.model.User;
import com.systeam.backend.UserAdministration.repository.RoleRepository;
import com.systeam.backend.UserAdministration.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    //Conexion con tabla de usuario
    private final UserRepository userRepository;
    //Conexion con tabla de roles
    private final RoleRepository roleRepository;
    //Encoder para passwords 
    private final PasswordEncoder passwordEncoder;

    //===========================EVENTOS=============================
    //EVENTO: Crear un usuario
    public UserResponse createUser(CreateUserRequest request) {
        //Validar email único, sino ERROR
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado en IDEAFY");
        }

        // Le asignamos el Rol por defecto: INVESTOR
        //Busco el rol INVESTOR en la tabla de roles, sino ERROR.
        Role defaultRole = roleRepository.findByName("INVESTOR")
            .orElseThrow(() -> new RuntimeException("Rol INVESTOR no encontrado"));

        //Armo el usuario con password encoded, y con los datos recibidos y role por defecto INVESTOR
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .enabled(true)
            .roles(new HashSet<>(Set.of(defaultRole)))
            .build();

        //Mando a crear el usuario en la tabla de usuarios
        return toResponse(userRepository.save(user));
    }

    //EVENTO: Obtener todos los usuarios
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    //EVENTO: Obtener un usuario por ID
    public UserResponse getUserById(Long id) {
        return toResponse(findUserOrThrow(id));
    }

    //EVENTO: Actualizar un usuario
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        //Actualiza usuario por ID
        User user = findUserOrThrow(id);
        user.setName(request.getName());        user.setName(request.getName());
        user.setEmail(request.getEmail());
        return toResponse(userRepository.save(user));
    }

    //EVENTO: Deshabilitar un usuario
    public void disableUser(Long id) {
        User user = findUserOrThrow(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    // --- helpers ---
    //Buscar un usuario por ID, sino ERROR
    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    //Convertir un usuario a una respuesta para ser leida por el cliente
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .enabled(user.getEnabled())
            .roles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()))
            .createdAt(user.getCreatedAt())
            .build();
    }
}