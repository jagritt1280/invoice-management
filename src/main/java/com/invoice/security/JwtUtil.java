package com.invoice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    // reads jwt.secret from application.properties
    private String secret;

    @Value("${jwt.expiration}")
    // reads jwt.expiration from application.properties (86400000 = 24 hours)
    private Long expiration;

    // generate signing key from secret
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // generate token from email
    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email)          // store email in token
                .setIssuedAt(new Date())    // when token was created
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())  // sign with secret key
                .compact();
    }

    // extract email from token
    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();  // get email we stored
    }

    // validate token — checks signature + expiry
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch(JwtException | IllegalArgumentException e) {
            return false;
            // token is invalid, expired, or tampered
        }
    }
}