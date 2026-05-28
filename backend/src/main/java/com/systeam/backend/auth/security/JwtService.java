package com.systeam.backend.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.InvalidKeyException;

@Service
public class JwtService {

    @Value("${app.security.jwt.private-key}")
    private String privateKeyBase64;

    @Value("${app.security.jwt.public-key}")
    private String publicKeyBase64;

    @Value("${app.security.jwt.secret:}")
    private String legacySecret;

    @Value("${app.security.jwt.expiration-ms}")
    private long expirationMs;

    @Value("${app.security.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    // Generate JWT token con extra claims
    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) throws InvalidKeyException, Exception {
        //Tomo la fecha de ahora y pongo la expiración
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        var builder = Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration);

        return signJwt(builder);
    }

    // Generar Refresh Token (misma estructura pero mas largo y con tipo)
    public String generateRefreshToken(UserDetails userDetails, String tokenId) throws InvalidKeyException, Exception {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshExpirationMs);

        var builder = Jwts.builder()
                .id(tokenId) // El mismo token ID que guardamos en BD
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .claim("type", "refresh"); // <-- Diferenciamos el tipo como Refresh

        return signJwt(builder);
    }

    // Extraer el jti (JWT ID) del token
    public String extractTokenId(String token) throws JwtException, Exception {
        return extractClaims(token, Claims::getId);
    }

    // Verificar el Refresh Token
    public boolean isRefreshToken(String token) throws Exception {
        try {
            Claims claims = extractAllClaims(token);
            String type = (String) claims.get("type");
            return "refresh".equals(type);
        } catch (Exception e) {
            return false;
        }
    }

    // Actualizar getExpirationMs() para que devuelva ambos
    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    // Extrae el username basandose en un token
    public String extractUsername(String token) throws JwtException, IllegalArgumentException, Exception {
        return extractClaims(token, Claims::getSubject);
    }

    // Compara el username del token con el username del UserDetails y si el token
    // ha expirado
    public boolean isTokenValid(String token, UserDetails userDetails)
            throws JwtException, IllegalArgumentException, Exception {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Comprueba si el token ha expirado
    private boolean isTokenExpired(String token) throws JwtException, IllegalArgumentException, Exception {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver)
            throws JwtException, IllegalArgumentException, Exception {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extracts all claims (payload data) from the JWT token:
    // - subject (username)
    // - issuedAt (creation timestamp)
    // - expiration (expiry timestamp)
    // - any custom extra claims (roles, permissions, etc.)
    private Claims extractAllClaims(String token) throws JwtException, IllegalArgumentException, Exception {
        String alg = extractAlg(token);

        if (alg != null && alg.startsWith("HS")) {
            return Jwts.parser()
                    .verifyWith(getHmacSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }

        if (hasRsaPrivateKey()) {
            return Jwts.parser()
                    .verifyWith(getVerificationKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }

        return Jwts.parser()
                .verifyWith(getHmacSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Retorna la clave privada para firmar (admite DER base64, PEM y PEM codificado
    // en base64)
    private PrivateKey getSigningKey() throws Exception {
        byte[] keyBytes = decodeKeyMaterial(privateKeyBase64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    private SecretKey getHmacSigningKey() {
        String hmacSecret = resolveHmacSecret();
        if (hmacSecret == null || hmacSecret.trim().isEmpty()) {
            throw new IllegalArgumentException("La clave JWT secreta no puede estar vacia");
        }
        return new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private SignatureAlgorithm resolveSignatureAlgorithm() {
        return hasRsaPrivateKey() ? SignatureAlgorithm.RS256 : SignatureAlgorithm.HS256;
    }

    private Key resolveSigningKey() throws Exception {
        return hasRsaPrivateKey() ? getSigningKey() : getHmacSigningKey();
    }

    private boolean hasRsaPrivateKey() {
        return privateKeyBase64 != null && !privateKeyBase64.trim().isEmpty();
    }

    private String resolveHmacSecret() {
        if (legacySecret != null && !legacySecret.trim().isEmpty()) {
            return legacySecret;
        }
        // Fallback de compatibilidad para entornos donde RSA viene mal configurado.
        if (privateKeyBase64 != null && !privateKeyBase64.trim().isEmpty()) {
            return privateKeyBase64;
        }
        return null;
    }

    private String signJwt(io.jsonwebtoken.JwtBuilder builder) throws Exception {
        if (hasRsaPrivateKey()) {
            try {
                return builder
                        .signWith(getSigningKey(), SignatureAlgorithm.RS256)
                        .compact();
            } catch (Exception e) {
                // Si RSA falla por material de clave inválido, degradamos a HS para no romper login.
            }
        }

        return builder
                .signWith(getHmacSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String extractAlg(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            byte[] headerBytes = Base64.getUrlDecoder().decode(parts[0]);
            String headerJson = new String(headerBytes, StandardCharsets.UTF_8);
            int idx = headerJson.indexOf("\"alg\"");
            if (idx < 0) {
                return null;
            }
            int colon = headerJson.indexOf(':', idx);
            int firstQuote = headerJson.indexOf('"', colon + 1);
            int secondQuote = headerJson.indexOf('"', firstQuote + 1);
            if (colon < 0 || firstQuote < 0 || secondQuote < 0) {
                return null;
            }
            return headerJson.substring(firstQuote + 1, secondQuote);
        } catch (Exception e) {
            return null;
        }
    }

    private PublicKey getVerificationKey() throws Exception {
        // Para evitar desalineaciones entre .env (public/private distintos),
        // derivamos siempre la pública desde la privada usada para firmar.
        PrivateKey privateKey = getSigningKey();
        RSAPrivateCrtKey rsaPrivate = (RSAPrivateCrtKey) privateKey;
        RSAPublicKeySpec publicSpec = new RSAPublicKeySpec(
                rsaPrivate.getModulus(),
                rsaPrivate.getPublicExponent());
        return KeyFactory.getInstance("RSA").generatePublic(publicSpec);
    }

    private byte[] decodeKeyMaterial(String keyValue) {
        String normalized = keyValue == null ? "" : keyValue.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("La clave JWT no puede estar vacia");
        }

        // Caso 1: llega como PEM literal.
        if (normalized.contains("-----BEGIN")) {
            return decodePem(normalized);
        }

        // Caso 2: llega como base64 DER o como base64 de un PEM completo.
        byte[] firstPass = Base64.getDecoder().decode(normalized);
        String firstPassAsText = new String(firstPass, StandardCharsets.UTF_8);
        if (firstPassAsText.contains("-----BEGIN")) {
            return decodePem(firstPassAsText);
        }
        return firstPass;
    }

    private byte[] decodePem(String pemText) {
        String cleanPem = pemText
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(cleanPem);
    }

    // Retorna la expiración en milisegundos
    public long getExpirationMs() {
        return expirationMs;
    }

}
