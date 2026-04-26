package com.systeam.backend.auth.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.systeam.backend.UserAdministration.model.Role;
import com.systeam.backend.UserAdministration.model.User;
import com.systeam.backend.UserAdministration.repository.RoleRepository;
import com.systeam.backend.UserAdministration.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String providerId = oauth2User.getAttribute("sub");
        String provider = userRequest.getClientRegistration().getRegistrationId();

        // Buscar usuario existente o crear nuevo
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            Role defaultRole = roleRepository.findByName("INVESTOR")
                .orElseThrow(() -> new RuntimeException("Rol INVESTOR no encontrado"));

            User newUser = User.builder()
                .name(name != null ? name : email)
                .email(email)
                .password(null)
                .provider(provider)
                .providerId(providerId)
                .enabled(true)
                .roles(new HashSet<>(Set.of(defaultRole)))
                .build();

            return userRepository.save(newUser);
        });

        // Actualizar provider y providerId si el usuario ya existe (para nuevos proveedores)
        boolean needsUpdate = false;
        if (user.getProvider() == null || !user.getProvider().equals(provider)) {
            user.setProvider(provider);
            needsUpdate = true;
        }
        if (user.getProviderId() == null || !user.getProviderId().equals(providerId)) {
            user.setProviderId(providerId);
            needsUpdate = true;
        }
        if (needsUpdate) {
            user = userRepository.save(user);
        }

        return oauth2User;
    }
}
