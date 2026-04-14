package com.systeam.backend.UserAdministration.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.systeam.backend.UserAdministration.dto.CreatePermissionRequest;
import com.systeam.backend.UserAdministration.dto.PermissionResponse;
import com.systeam.backend.UserAdministration.dto.UpdatePermissionRequest;
import com.systeam.backend.UserAdministration.model.Permission;
import com.systeam.backend.UserAdministration.repository.PermissionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermissionService {
    //Link con la tabla
    private final PermissionRepository permissionRepository;
    
    //EVENTO: CREAR UN PERMISO
    public PermissionResponse create(CreatePermissionRequest request) {
        if (permissionRepository.existsByName(request.getName())) {
            throw new RuntimeException("Ya existe un permiso con ese nombre");
        }

        Permission permission = Permission.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return toResponse(permissionRepository.save(permission));
    }
    //EVENTO: OBTENER TODOS LOS PERMISOS
    public List<PermissionResponse> findAll() {
        return permissionRepository.findAll().stream().map(this::toResponse).toList();
    }
    //EVENTO: OBTENER PERMISO POR ID
    public PermissionResponse findById(Long id) {
        return toResponse(getPermission(id));
    }

    //EVENTO: ACTUALIZAR PERMISO POR ID
    public PermissionResponse update(Long id, UpdatePermissionRequest request) {
        Permission permission = getPermission(id);
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        return toResponse(permissionRepository.save(permission));
    }

    //EVENTO: ELIMINAR PERMISO POR ID
    public void delete(Long id) {
        permissionRepository.delete(getPermission(id));
    }

    //EVENTO: OBTENER PERMISO 
    private Permission getPermission(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Permiso no encontrado"));
    }
    
    private PermissionResponse toResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .build();
    }
}
