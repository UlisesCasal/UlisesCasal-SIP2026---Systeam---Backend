package com.systeam.backend.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

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

    @Value("${app.security.jwt.expiration-ms}")
    private long expirationMs;

    // Generate JWT token con extra claims
    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) throws InvalidKeyException, Exception {
        //Tomo la fecha de ahora y pongo la expiración
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        //Creo el token
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey()
            , SignatureAlgorithm.RS256)
            .compact();
    }

    //Extrae el username basandose en un token
    public String extractUsername(String token) throws JwtException, IllegalArgumentException, Exception {
        return extractClaims(token, Claims::getSubject);
    }

    //Compara el username del token con el username del UserDetails y si el token ha expirado
    public boolean isTokenValid(String token, UserDetails userDetails) throws JwtException, IllegalArgumentException, Exception {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    //Comprueba si el token ha expirado
    private boolean isTokenExpired(String token) throws JwtException, IllegalArgumentException, Exception {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) throws JwtException, IllegalArgumentException, Exception{
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    // Extracts all claims (payload data) from the JWT token:
    // - subject (username)
    // - issuedAt (creation timestamp)
    // - expiration (expiry timestamp)
    // - any custom extra claims (roles, permissions, etc.)
      private Claims extractAllClaims(String token) throws JwtException, IllegalArgumentException, Exception {
        return Jwts.parser()
                .verifyWith(getVerificationKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Retorna la clave privada para firmar (admite DER base64, PEM y PEM codificado en base64)
    private PrivateKey getSigningKey() throws Exception {
        byte[] keyBytes = decodeKeyMaterial(privateKeyBase64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
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

    //Retorna la expiración en milisegundos
    public long getExpirationMs() {
        return expirationMs;
    }

}
