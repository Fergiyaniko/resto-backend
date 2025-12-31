package com.example.resto_backend.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration-ms}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration-ms}") long refreshTokenExpiration) {

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /* ===================== TOKEN GENERATION ===================== */

    public String generateAccessToken(String username) {
        return generateToken(username, accessTokenExpiration);
    }

    public String generateRefreshToken(String username) {
        return generateToken(username, refreshTokenExpiration);
    }

    private String generateToken(String username, long expirationMillis) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /* ===================== TOKEN PARSING ===================== */

    public String extractUsername(String token, boolean ignoreExpiration) {
        return extractAllClaims(token, ignoreExpiration).getSubject();
    }

    private Claims extractAllClaims(String token, boolean ignoreExpiration) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            if (ignoreExpiration) {
                return e.getClaims();
            }
            throw e;
        }
    }

    public boolean validateToken(String token, boolean ignoreExpiration) {
        try {
            extractAllClaims(token, ignoreExpiration);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
