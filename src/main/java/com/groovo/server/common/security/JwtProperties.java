package com.groovo.server.common.security;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
	String secret,
	Duration wsTokenExpiration
) {
	private static final int MIN_SECRET_BYTES = 32;
	private static final Duration DEFAULT_WS_TOKEN_EXPIRATION = Duration.ofMinutes(30);

	public JwtProperties {
		if (secret == null || secret.isBlank()) {
			throw new IllegalArgumentException("app.jwt.secret must not be blank");
		}
		if (secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
			throw new IllegalArgumentException("app.jwt.secret must be at least 32 bytes for HS256");
		}
		if (wsTokenExpiration == null) {
			wsTokenExpiration = DEFAULT_WS_TOKEN_EXPIRATION;
		}
		if (wsTokenExpiration.isZero() || wsTokenExpiration.isNegative()) {
			throw new IllegalArgumentException("app.jwt.ws-token-expiration must be positive");
		}
	}
}
