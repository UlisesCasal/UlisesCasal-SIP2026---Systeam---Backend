package com.systeam.backend.UserAdministration.controller;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.systeam.backend.UserAdministration.dto.CreateRoleRequest;
import com.systeam.backend.UserAdministration.dto.RoleResponse;
import com.systeam.backend.UserAdministration.dto.UpdateRoleRequest;
import com.systeam.backend.UserAdministration.service.RoleService;

import jakarta.validation.Valid;


//Se expone el ENDPOINT para adminsitrar los roles
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasAuthority('role:create')")
    public RoleResponse create(@RequestBody @Valid CreateRoleRequest request) {
        return roleService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('role:read')")
    public List<RoleResponse> findAll() {
        return roleService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('role:read')")
    public RoleResponse findById(@PathVariable Long id) {
        return roleService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:update')")
    public RoleResponse update(@PathVariable Long id, @RequestBody @Valid UpdateRoleRequest request) {
        return roleService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        roleService.delete(id);
    }

    //=================permisos==========================
    //EVENTO PARA ASIGNAR UN PERMISO A UN ROLE
    @PostMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('role:update')")
    public RoleResponse assignPermission(
        @PathVariable Long roleId,
        @PathVariable Long permissionId
    ) {
        return roleService.assignPermission(roleId, permissionId);
    }
    //EVENTO PARA REVOCAR UN PERMISO
    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('role:update')")
    public RoleResponse revokePermission(
        @PathVariable Long roleId,
        @PathVariable Long permissionId
    ) {
        return roleService.revokePermission(roleId, permissionId);
    }
}
