package com.groovo.server.common.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class JwtProviderTest {
	private static final String SECRET = "test-secret-key-for-jwt-signing-0123456789";

	@Test
	void create_signsTokenWithSubjectClaimsAndExpiry() {
		JwtProvider provider = new JwtProvider(new JwtProperties(SECRET, Duration.ofMinutes(30)));

		String token = provider.create("session-id-1", Map.of("userId", 7L, "videoId", 42L), Duration.ofMinutes(30));

		SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
		Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
		assertThat(claims.getSubject()).isEqualTo("session-id-1");
		assertThat(((Number) claims.get("userId")).longValue()).isEqualTo(7L);
		assertThat(((Number) claims.get("videoId")).longValue()).isEqualTo(42L);
		assertThat(claims.getExpiration()).isAfter(Date.from(Instant.now()));
	}
}
