package com.systeam.backend.auth.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.systeam.backend.UserAdministration.model.User;
import com.systeam.backend.UserAdministration.repository.UserRepository;

import lombok.RequiredArgsConstructor;
//DEVUELVE AUTHORITIES POR ROL Y PERMISO DE UN USUARIO DADO SU EMAIL

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    //Link con tabla
    private final UserRepository userRepository;

    //Metodo que recibe un email: Retorna un UserDetails
    @Override
    public UserDetails loadUserByUsername(String email) {
        //Busca el usuario por email
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        //Verifica si el usuario está habilitado
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new DisabledException("Usuario desactivado");
        }

        //Obtiene los roles y permisos del usuario
        Set<GrantedAuthority> authorities = new HashSet<>();
        //Recorre los roles del usuario y sus permisos para agregarlos a authorities
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            role.getPermissions().forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getName()))
            );
        });
        //Retorna UserDetails con el usuario y sus roles y permisos y authorities
        String password = user.getPassword() != null ? user.getPassword() : "";
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(password)
            .authorities(authorities)
            .disabled(!user.getEnabled())
            .build();
    }
}