package com.systeam.backend.UserAdministration.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.systeam.shared.model.Permiso;

public interface PermissionRepository extends JpaRepository<Permiso, Long> {
    Optional<Permiso> findByName(String name);
    boolean existsByName(String name);
}
