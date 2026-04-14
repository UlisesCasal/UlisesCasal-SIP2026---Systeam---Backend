package com.systeam.backend.UserAdministration.controller;

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

import com.systeam.backend.UserAdministration.dto.CreatePermissionRequest;
import com.systeam.backend.UserAdministration.dto.PermissionResponse;
import com.systeam.backend.UserAdministration.dto.UpdatePermissionRequest;
import com.systeam.backend.UserAdministration.service.PermissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {
    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasAuthority('permission:create')")
    public PermissionResponse create(@RequestBody @Valid CreatePermissionRequest request) {
        return permissionService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('permission:read')")
    public List<PermissionResponse> findAll() {
        return permissionService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:read')")
    public PermissionResponse findById(@PathVariable Long id) {
        return permissionService.findById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:update')")
    public PermissionResponse update(@PathVariable Long id, @RequestBody @Valid UpdatePermissionRequest request) {
        return permissionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('permission:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        permissionService.delete(id);
    }
}
