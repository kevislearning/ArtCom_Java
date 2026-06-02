package com.artgallery.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final long jwtExpirationInMs = 30L * 24 * 60 * 60 * 1000; // 30 days

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret) {
        // Enforce secret length for HS256 (needs at least 256 bits / 32 bytes)
        byte[] secretBytes = jwtSecret.getBytes();
        if (secretBytes.length < 32) {
            // pad or generate secure key
            this.key = Keys.hmacShaKeyFor((jwtSecret + "paddingpaddingpaddingpaddingpadding").getBytes());
        } else {
            this.key = Keys.hmacShaKeyFor(secretBytes);
        }
    }

    public String generateToken(UUID userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
            String subject = claims.getSubject();
            if (subject == null) {
                return false;
            }
            UUID.fromString(subject);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // log token error
        }
        return false;
    }
}
