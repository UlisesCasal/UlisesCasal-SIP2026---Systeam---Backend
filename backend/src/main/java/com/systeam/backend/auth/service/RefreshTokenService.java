package com.systeam.backend.auth.service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.systeam.backend.UserAdministration.model.User;
import com.systeam.backend.UserAdministration.repository.UserRepository;
import com.systeam.backend.auth.model.RefreshToken;
import com.systeam.backend.auth.repository.RefreshTokenRepository;
import com.systeam.backend.auth.security.CustomUserDetailsService;
import com.systeam.backend.auth.security.JwtService;

import io.jsonwebtoken.security.InvalidKeyException;
import jakarta.validation.ValidationException;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            UserRepository userRepository,
            CustomUserDetailsService userDetailsService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(
            Instant.now().plusMillis(jwtService.getRefreshExpirationMs())
        );

        return refreshTokenRepository.save(refreshToken);
    }

    @Transactional
    public com.systeam.backend.auth.dto.LoginResponse refreshTokenPair(String refreshTokenJwt) throws Exception {
        if (!jwtService.isRefreshToken(refreshTokenJwt)) {
            throw new ValidationException("Token no es un refresh token válido");
        }

        String tokenId = jwtService.extractTokenId(refreshTokenJwt);

        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenIdAndRevokedFalse(tokenId)
                .orElseThrow(() -> new ValidationException("Refresh token no encontrado o revocado"));

        if (refreshToken.isExpired()) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new ValidationException("Refresh token expirado");
        }

        User user = refreshToken.getUser();
        var userDetails = userDetailsService.loadUserByUsername(user.getEmail());

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        String newAccessToken = jwtService.generateToken(userDetails, Map.of(
                "userId", user.getId(),
                "roles", roles,
                "permissions", permissions
        ));

        RefreshToken newRefreshToken = createRefreshToken(user);
        String newRefreshTokenJwt = jwtService.generateRefreshToken(userDetails, newRefreshToken.getTokenId());

        return com.systeam.backend.auth.dto.LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenJwt)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationMs())
                .userId(user.getId())
                .email(user.getEmail())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Transactional
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
    }
}
