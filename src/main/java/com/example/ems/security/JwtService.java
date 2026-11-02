package com.example.ems.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expiration;

    public JwtService(@Value("${ems.jwt.secret}") String secret, @Value("${ems.jwt.expiration-ms}") long expiration) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("ems.jwt.secret must be at least 32 bytes");
        
        }this.key = Keys.hmacShaKeyFor(bytes);
        this.expiration = expiration;
    }

    public String generate(String email, String role) {
        Date now = new Date();
        return Jwts.builder().subject(email).claim("role", role).issuedAt(now).expiration(new Date(now.getTime() + expiration)).signWith(key).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
