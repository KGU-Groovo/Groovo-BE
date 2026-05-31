package com.groovo.server.common.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
	private final SecretKey key;
	private final JwtProperties properties;

	public JwtProvider(JwtProperties properties) {
		this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
		this.properties = properties;
	}

	public String generateToken(Long userId) {
		return create(String.valueOf(userId), Map.of(), properties.accessTokenExpiration());
	}

	public String create(String subject, Map<String, Object> claims, Duration ttl) {
		if (ttl == null || ttl.isZero() || ttl.isNegative()) {
			throw new IllegalArgumentException("ttl must be positive");
		}

		Instant now = Instant.now();
		return Jwts.builder()
			.claims(claims)
			.subject(subject)
			.issuedAt(Date.from(now))
			.expiration(Date.from(now.plus(ttl)))
			.signWith(key)
			.compact();
	}

	public Long getUserId(String token) {
		return Long.parseLong(getClaims(token).getSubject());
	}

	public boolean validate(String token) {
		try {
			getClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	public long getExpiresIn() {
		return properties.accessTokenExpiration().toSeconds();
	}

	private Claims getClaims(String token) {
		return Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
