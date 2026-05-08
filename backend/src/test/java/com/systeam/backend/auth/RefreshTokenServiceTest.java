package com.systeam.backend.auth;

import com.systeam.shared.model.Usuario;
import com.systeam.backend.UserAdministration.repository.UserRepository;
import com.systeam.backend.auth.model.RefreshToken;
import com.systeam.backend.auth.repository.RefreshTokenRepository;
import com.systeam.backend.auth.security.CustomUserDetailsService;
import com.systeam.backend.auth.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    private RefreshTokenRepository refreshTokenRepository;
    private JwtService jwtService;
    private UserRepository userRepository;
    private CustomUserDetailsService userDetailsService;
    private com.systeam.backend.auth.service.RefreshTokenService refreshTokenService;

    private Usuario testUser;
    private String validTokenId = "test-token-id-123";

    @BeforeEach
    void setUp() {
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        jwtService = mock(JwtService.class);
        userRepository = mock(UserRepository.class);
        userDetailsService = mock(CustomUserDetailsService.class);
        
        // Crear el servicio MANUALMENTE (sin @InjectMocks)
        refreshTokenService = new com.systeam.backend.auth.service.RefreshTokenService(
                refreshTokenRepository, jwtService, userRepository, userDetailsService
        );

        testUser = new Usuario();
        testUser.setId(1L);
        testUser.setEmail("test@systeam.com");

        // Mock para createRefreshToken - simular el save
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
            RefreshToken t = inv.getArgument(0);
            if (t.getId() == null) {
                t.setId(99L);
            }
            return t;
        });
    }

    @Test
    void refreshTokenPair_withValidToken_shouldReturnNewTokens() throws Exception {
        // Given
        String validJwt = "valid-jwt";
        RefreshToken validRefreshToken = new RefreshToken();
        validRefreshToken.setId(1L);
        validRefreshToken.setTokenId(validTokenId);
        validRefreshToken.setUser(testUser);
        validRefreshToken.setExpiryDate(Instant.now().plusSeconds(3600));
        validRefreshToken.setRevoked(false);

        when(jwtService.isRefreshToken(validJwt)).thenReturn(true);
        when(jwtService.extractTokenId(validJwt)).thenReturn(validTokenId);
        when(jwtService.getRefreshExpirationMs()).thenReturn(604800000L);
        when(refreshTokenRepository.findByTokenIdAndRevokedFalse(validTokenId))
                .thenReturn(Optional.of(validRefreshToken));
        when(userRepository.findByEmail("test@systeam.com")).thenReturn(Optional.of(testUser));
        
        UserDetails mockUserDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("test@systeam.com")).thenReturn(mockUserDetails);
        when(jwtService.generateToken(any(), any())).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(any(), anyString())).thenReturn("new-refresh-token");

        // When
        var response = refreshTokenService.refreshTokenPair(validJwt);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");

        // Verificar que el token viejo se revocó
        assertThat(validRefreshToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(1)).save(validRefreshToken);

        System.out.println("✅ Test pasado: Refresh token válido genera nuevo par");
    }

    @Test
    void refreshTokenPair_withRevokedToken_shouldThrowException() throws Exception {
        // Given
        String revokedJwt = "revoked-jwt";
        when(jwtService.isRefreshToken(revokedJwt)).thenReturn(true);
        when(jwtService.extractTokenId(revokedJwt)).thenReturn(validTokenId);
        when(refreshTokenRepository.findByTokenIdAndRevokedFalse(validTokenId))
                .thenReturn(Optional.empty()); // No encontrado porque está revocado

        // When/Then
        try {
            refreshTokenService.refreshTokenPair(revokedJwt);
            assertThat(false).isTrue(); // No debería llegar acá
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("no encontrado o revocado");
        }

        System.out.println("✅ Test pasado: Token revocado es rechazado");
    }

    @Test
    void refreshTokenPair_withExpiredToken_shouldThrowException() throws Exception {
        // Given
        String expiredJwt = "expired-jwt";
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setId(2L);
        expiredToken.setTokenId(validTokenId);
        expiredToken.setUser(testUser);
        expiredToken.setExpiryDate(Instant.now().minusSeconds(3600)); // Expirado
        expiredToken.setRevoked(false);

        when(jwtService.isRefreshToken(expiredJwt)).thenReturn(true);
        when(jwtService.extractTokenId(expiredJwt)).thenReturn(validTokenId);
        when(refreshTokenRepository.findByTokenIdAndRevokedFalse(validTokenId))
                .thenReturn(Optional.of(expiredToken));

        // When/Then
        try {
            refreshTokenService.refreshTokenPair(expiredJwt);
            assertThat(false).isTrue(); // No debería llegar acá
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("expirado");
            assertThat(expiredToken.isRevoked()).isTrue(); // Debe revocarse aunque expire
        }

        System.out.println("✅ Test pasado: Token expirado es rechazado y revocado");
    }

    @Test
    void refreshTokenPair_withInvalidJwt_shouldThrowException() throws Exception {
        // Given
        String invalidJwt = "invalid-jwt";
        when(jwtService.isRefreshToken(invalidJwt)).thenReturn(false);

        // When/Then
        try {
            refreshTokenService.refreshTokenPair(invalidJwt);
            assertThat(false).isTrue(); // No debería llegar acá
        } catch (Exception e) {
            assertThat(e.getMessage()).contains("no es un refresh token válido");
        }

        System.out.println("✅ Test pasado: JWT inválido es rechazado");
    }

    @Test
    void createRefreshToken_shouldCreateNewToken() {
        // Given
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
            RefreshToken t = inv.getArgument(0);
            t.setId(99L);
            return t;
        });

        // When
        RefreshToken newToken = refreshTokenService.createRefreshToken(testUser);

        // Then
        assertThat(newToken).isNotNull();
        assertThat(newToken.getUser()).isEqualTo(testUser);
        assertThat(newToken.isRevoked()).isFalse();
        // Verificar que la expiración es en el futuro (con margen de 1 segundo)
        assertThat(newToken.getExpiryDate().isAfter(Instant.now().minusSeconds(1))).isTrue();

        // Verificar que se borraron los tokens anteriores del usuario
        verify(refreshTokenRepository, times(1)).deleteByUser(testUser);

        System.out.println("✅ Test pasado: Creación de refresh token funciona");
    }
}
