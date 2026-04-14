package com.systeam.backend.UserAdministration.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.systeam.backend.UserAdministration.dto.CreateRoleRequest;
import com.systeam.backend.UserAdministration.dto.RoleResponse;
import com.systeam.backend.UserAdministration.dto.UpdateRoleRequest;
import com.systeam.backend.UserAdministration.model.Permission;
import com.systeam.backend.UserAdministration.model.Role;
import com.systeam.backend.UserAdministration.repository.PermissionRepository;
import com.systeam.backend.UserAdministration.repository.RoleRepository;
import com.systeam.backend.UserAdministration.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    //EVENTO: CREAR UN ROL
    public RoleResponse create(CreateRoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new RuntimeException("Ya existe un rol con ese nombre");
        }
        //Se crea el rol
        Role role = Role.builder()
            .name(request.getName())
            .description(request.getDescription())
            .build();

        return toResponse(roleRepository.save(role));
    }
    //Trae todos los roles
    public List<RoleResponse> findAll() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }
    //Busca un role por ID
    public RoleResponse findById(Long id) {
        return toResponse(getRole(id));
    }
    //Actualiza un rol
    public RoleResponse update(Long id, UpdateRoleRequest request) {
        Role role = getRole(id);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        return toResponse(roleRepository.save(role));
    }
    //Elimina un rol
    public void delete(Long id) {
        if (userRepository.existsByRoles_Id(id)) {
            throw new RuntimeException("No se puede borrar un rol con usuarios asignados");
        }
        roleRepository.delete(getRole(id));
    }
    //Obtiene un rol en base a un ID
    private Role getRole(Long id) {
        return roleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
    }
    //Mapea el role a la estructura de respuesta
    private RoleResponse toResponse(Role role) {
        return RoleResponse.builder()
            .id(role.getId())
            .name(role.getName())
            .description(role.getDescription())
            .permissions(
                role.getPermissions().stream().map(permission -> permission.getName()).collect(java.util.stream.Collectors.toSet())
            )
            .build();
    }

    //=======================PERMISOS===========================

    public RoleResponse assignPermission(Long roleId, Long permissionId) {
        Role role = getRole(roleId);
        Permission permission = permissionRepository.findById(permissionId)
            .orElseThrow(() -> new RuntimeException("Permiso no encontrado"));

        role.getPermissions().add(permission);
        return toResponse(roleRepository.save(role));
    }

    public RoleResponse revokePermission(Long roleId, Long permissionId) {
        Role role = getRole(roleId);
        role.getPermissions().removeIf(permission -> permission.getId().equals(permissionId));
        return toResponse(roleRepository.save(role));
    }
}
