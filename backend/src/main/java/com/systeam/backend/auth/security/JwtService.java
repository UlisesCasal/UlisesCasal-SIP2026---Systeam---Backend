package com.systeam.backend.auth.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Service
public class JwtService {

    @Value("${app.security.jwt.secret}")
    private String secret;

    @Value("${app.security.jwt.expiration-ms}")
    private long expirationMs;

    // Generate JWT token con extra claims
    public String generateToken(UserDetails userDetails, Map<String, Object> extraClaims) {
        //Tomo la fecha de ahora y pongo la expiración
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);

        //Creo el token
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    //Extrae el username basandose en un token
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    //Compara el username del token con el username del UserDetails y si el token ha expirado
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    //Comprueba si el token ha expirado
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    
    // Extracts all claims (payload data) from the JWT token:
    // - subject (username)
    // - issuedAt (creation timestamp)
    // - expiration (expiry timestamp)
    // - any custom extra claims (roles, permissions, etc.)
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith((javax.crypto.SecretKey) getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    //Retorna la clave de firma para verificar el token
    private Key getSigningKey() {
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    //Retorna la expiración en milisegundos
    public long getExpirationMs() {
        return expirationMs;
    }

}
