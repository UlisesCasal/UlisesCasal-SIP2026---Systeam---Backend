package com.systeam.backend.auth.security;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.systeam.shared.model.Usuario;
import com.systeam.backend.UserAdministration.repository.UserRepository;
import com.systeam.backend.auth.model.RefreshToken;
import com.systeam.backend.auth.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                          Authentication authentication) throws IOException {
        
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        Usuario user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario OAuth2 no encontrado"));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        Set<String> roles = user.getRoles().stream()
            .map(r -> r.getName())
            .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
            .flatMap(r -> r.getPermissions().stream())
            .map(p -> p.getName())
            .collect(Collectors.toSet());

        try {
            // Generar access token
            String accessToken = jwtService.generateToken(userDetails, Map.of(
                "userId", user.getId(),
                "roles", roles,
                "permissions", permissions
            ));

            // Generar refresh token
            RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(user);
            String refreshToken = jwtService.generateRefreshToken(userDetails, refreshTokenEntity.getTokenId());

            // Redirigir con ambos tokens
            response.sendRedirect(redirectUri + "?accessToken=" + accessToken + "&refreshToken=" + refreshToken);
        } catch (Exception e) {
            response.sendRedirect(redirectUri + "?error=token_generation_failed");
        }
    }
}
