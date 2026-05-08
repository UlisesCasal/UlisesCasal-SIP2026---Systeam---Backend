package com.systeam.backend.UserAdministration.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.systeam.shared.model.Rol;

@Repository
public interface RoleRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByName(String name);
    boolean existsByName(String name);
}