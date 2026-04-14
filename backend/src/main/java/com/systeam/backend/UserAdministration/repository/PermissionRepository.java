package com.systeam.backend.UserAdministration.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.systeam.backend.UserAdministration.model.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    boolean existsByName(String name);
}
