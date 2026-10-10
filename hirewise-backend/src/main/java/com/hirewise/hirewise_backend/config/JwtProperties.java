package com.hirewise.hirewise_backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class JwtProperties {

    @Value("${hirewise.jwt.secret:}")
    private String secret;

    @Value("${hirewise.jwt.expiration-ms:86400000}")
    private long expirationMs;

    @PostConstruct
    public void validate() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException(
                "JWT_SECRET environment variable is missing or empty. Please set a secure secret of at least 32 characters (256 bits)."
            );
        }

        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                "JWT_SECRET is too short. HMAC-SHA256 requires at least 32 bytes (256 bits). Current length: " + secretBytes.length + " bytes."
            );
        }
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }
}
