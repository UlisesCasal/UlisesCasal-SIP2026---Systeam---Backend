package com.systeam.backend.UserAdministration.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.systeam.backend.UserAdministration.dto.CreateUserRequest;
import com.systeam.backend.UserAdministration.dto.UpdateUserRequest;
import com.systeam.backend.UserAdministration.dto.UserResponse;
import com.systeam.backend.UserAdministration.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    //ESTO ES LO QUE LE PEGARA EL CLIENTE
    //EL ENDPOINT ESTARA ESCUCHANDO EN /api/users
    private final UserService userService;

    //EVENTO: Crear un usuario se valida que el request body sea válido si el cliente hace un POST
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@RequestBody @Valid CreateUserRequest request) {
        return userService.createUser(request);
    }

    //EVENTO: Obtener todos los usuarios con paginación. Si el cliente pega un GET
    @GetMapping
    public Page<UserResponse> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return userService.getAllUsers(PageRequest.of(page, size));
    }
    //EVENTO: Obtener un usuario por ID. Si el cliente pega un GET con ID
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
    //EVENTO: Actualizar un usuario. Si el cliente pega un PUT con ID y request body válido
    @PutMapping("/{id}")
    public UserResponse updateUser(
        @PathVariable Long id,
        @RequestBody @Valid UpdateUserRequest request
    ) {
        return userService.updateUser(id, request);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disableUser(@PathVariable Long id) {
        userService.disableUser(id);
    }
}
