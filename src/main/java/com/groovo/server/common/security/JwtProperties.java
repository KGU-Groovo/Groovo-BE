package com.groovo.server.common.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
	String secret,
	Duration wsTokenExpiration
) {
	private static final Duration DEFAULT_WS_TOKEN_EXPIRATION = Duration.ofMinutes(30);

	public JwtProperties {
		if (wsTokenExpiration == null) {
			wsTokenExpiration = DEFAULT_WS_TOKEN_EXPIRATION;
		}
	}
}
