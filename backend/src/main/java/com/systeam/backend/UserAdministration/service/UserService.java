package com.systeam.backend.UserAdministration.service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import com.systeam.backend.UserAdministration.dto.CreateUserRequest;
import com.systeam.backend.UserAdministration.dto.UpdateUserRequest;
import com.systeam.backend.UserAdministration.dto.UserResponse;
import com.systeam.shared.model.Permiso;
import com.systeam.shared.model.Rol;
import com.systeam.shared.model.Usuario;
import com.systeam.backend.UserAdministration.repository.RoleRepository;
import com.systeam.backend.UserAdministration.repository.UserRepository;



@Service
@org.springframework.transaction.annotation.Transactional
public class UserService {
    // Conexion con tabla de usuario
    private final UserRepository userRepository;
    // Conexion con tabla de roles
    private final RoleRepository roleRepository;
    // Encoder para passwords
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public UserService(UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    // ===========================EVENTOS=============================
    // EVENTO: Crear un usuario
    public UserResponse createUser(CreateUserRequest request) {
        // Validar email único, sino ERROR
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado en IDEAFY");
        }

        // Le asignamos el Rol por defecto: INVESTOR
        // Busco el rol INVESTOR en la tabla de roles, sino ERROR.
        Rol defaultRole = roleRepository.findByName("INVESTOR")
                .orElseThrow(() -> new RuntimeException("Rol INVESTOR no encontrado"));

        // Armo el usuario con password encoded, y con los datos recibidos y role por
        // defecto INVESTOR
        Usuario user = Usuario.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fechaNacimiento(request.getFechaNacimiento())
                .provider("local")
                .enabled(true)
                .roles(new HashSet<>(Set.of(defaultRole)))
                .build();

        // Mando a crear el usuario en la tabla de usuarios
        return toResponse(userRepository.save(user));
    }

    // EVENTO: Obtener todos los usuarios
    public Page<UserResponse> getAllUsers(String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(search, search, pageable)
                    .map(this::toResponse);
        }
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    // EVENTO: Obtener un usuario por ID
    public UserResponse getUserById(Long id) {
        return toResponse(findUserOrThrow(id));
    }

    public com.systeam.backend.UserAdministration.dto.UserProfileResponse getProfile(Long id) {
        Usuario user = findUserOrThrow(id);
        String walletAddress = null;
        try {
            walletAddress = jdbcTemplate.queryForObject("SELECT wallet_address FROM users WHERE id = ?", String.class, id);
        } catch (Exception e) {
            // Ignorar si no hay wallet_address
        }
        return com.systeam.backend.UserAdministration.dto.UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .walletAddress(walletAddress)
                .createdAt(user.getCreatedAt())
                .build();
    }

    // EVENTO: Obtener usuario por email
    public UserResponse getCurrentUser(String email) {
        Usuario user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return toResponse(user);
    }

    // EVENTO: Actualizar un usuario
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        // Actualiza usuario por ID
        Usuario user = findUserOrThrow(id);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }
        if (request.getFechaNacimiento() != null) {
            user.setFechaNacimiento(request.getFechaNacimiento());
        }
        return toResponse(userRepository.save(user));
    }

    // EVENTO: Deshabilitar un usuario
    public void disableUser(Long id) {
        Usuario user = findUserOrThrow(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    // EVENTO: Cambio de contraseña:
    public void changePassword(String email, String currentPassword, String newPassword) {
        //Obtiene el usuario
        Usuario user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        //Comprueba si la contraseña actual es correcta, sino se lo devuelve
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }
        //Cambia la contraseña con el encoded
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // --- helpers ---
    // Buscar un usuario por ID, sino ERROR
    private Usuario findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // Convertir un usuario a una respuesta para ser leida por el cliente
    private UserResponse toResponse(Usuario user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .roles(user.getRoles().stream()
                        .map(Rol::getName)
                        .collect(Collectors.toSet()))
                .permissions(user.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(Permiso::getName)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .fechaNacimiento(user.getFechaNacimiento())
                .saldoIdea(user.getSaldoIdea())
                .saldoUsdt(user.getSaldoUsdt())
                .deletedAt(user.getDeletedAt())
                .build();
    }

    //Asignar rol a usuario
    public UserResponse assignRole(Long userId, Long roleId) {
        Usuario user = findUserOrThrow(userId);
        Rol role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));

        boolean yaAsignado = user.getRoles().stream()
            .anyMatch(r -> r.getId().equals(roleId));
        if (!yaAsignado) {
            user.getRoles().add(role);
        }
        return toResponse(userRepository.save(user));
    }
//Rovar rol de usuario
    public UserResponse revokeRole(Long userId, Long roleId) {
        Usuario user = findUserOrThrow(userId);
        user.getRoles().removeIf(role -> role.getId().equals(roleId));
        return toResponse(userRepository.save(user));
    }
}
