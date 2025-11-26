package com.fraudengine.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static com.fraudengine.security.JwtExceptions.*;

@Service
public class JwtService implements InitializingBean {

    private final String secret;
    private Key signingKey;

    public JwtService(@Value("${security.jwt.secret:}") String secret) {
        // Resolve secret from application properties; can be overridden by env var SECURITY_JWT_SECRET
        // We also map YAML to read from env JWT_SECRET to keep ops-friendly naming.
        this.secret = secret;
    }

    @Override
    public void afterPropertiesSet() {
        if (secret == null || secret.isBlank()) {
            throw new MissingSecretException("JWT_SECRET environment variable not set");
        }
        if (secret.length() < 32) { // simple length check for HS256 key strength
            throw new MissingSecretException("JWT_SECRET must be at least 32 characters long");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String subject, Duration ttl) {
        Instant now = Instant.now();
        Instant exp = now.plus(ttl);
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseAndValidate(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
        } catch (ExpiredJwtException e) {
            throw new ExpiredTokenException("Token expired");
        } catch (JwtException e) {
            throw new InvalidTokenException("Invalid JWT token");
        }
    }

    public Optional<String> extractSubject(String token) {
        try {
            return Optional.of(parseAndValidate(token).getBody().getSubject());
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
