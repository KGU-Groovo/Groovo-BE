package com.groovo.server.common.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    JwtProvider provider =
        new JwtProvider(new JwtProperties(SECRET, Duration.ofMinutes(30), Duration.ofMinutes(30)));

    String token =
        provider.create(
            "session-id-1", Map.of("userId", 7L, "videoId", 42L), Duration.ofMinutes(30));

    SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    assertThat(claims.getSubject()).isEqualTo("session-id-1");
    assertThat(((Number) claims.get("userId")).longValue()).isEqualTo(7L);
    assertThat(((Number) claims.get("videoId")).longValue()).isEqualTo(42L);
    assertThat(claims.getExpiration()).isAfter(Date.from(Instant.now()));
  }

  @Test
  void jwtProperties_defaultsWsTokenExpirationWhenNull() {
    JwtProperties properties = new JwtProperties(SECRET, null, null);

    assertThat(properties.wsTokenExpiration()).isEqualTo(Duration.ofMinutes(30));
  }

  @Test
  void jwtProperties_rejectsBlankSecret() {
    assertThatThrownBy(() -> new JwtProperties(" ", Duration.ofMinutes(30), Duration.ofMinutes(30)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("app.jwt.secret");
  }

  @Test
  void jwtProperties_rejectsShortSecret() {
    assertThatThrownBy(
            () -> new JwtProperties("short-secret", Duration.ofMinutes(30), Duration.ofMinutes(30)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("at least 32 bytes");
  }

  @Test
  void jwtProperties_rejectsZeroWsTokenExpiration() {
    assertThatThrownBy(() -> new JwtProperties(SECRET, Duration.ZERO, Duration.ofMinutes(30)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("app.jwt.ws-token-expiration");
  }

  @Test
  void jwtProperties_rejectsNegativeWsTokenExpiration() {
    assertThatThrownBy(
            () -> new JwtProperties(SECRET, Duration.ofSeconds(-1), Duration.ofMinutes(30)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("app.jwt.ws-token-expiration");
  }

  @Test
  void create_rejectsNullTtl() {
    JwtProvider provider =
        new JwtProvider(new JwtProperties(SECRET, Duration.ofMinutes(30), Duration.ofMinutes(30)));

    assertThatThrownBy(() -> provider.create("session-id-1", Map.of(), null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ttl");
  }

  @Test
  void create_rejectsZeroTtl() {
    JwtProvider provider =
        new JwtProvider(new JwtProperties(SECRET, Duration.ofMinutes(30), Duration.ofMinutes(30)));

    assertThatThrownBy(() -> provider.create("session-id-1", Map.of(), Duration.ZERO))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ttl");
  }

  @Test
  void create_rejectsNegativeTtl() {
    JwtProvider provider =
        new JwtProvider(new JwtProperties(SECRET, Duration.ofMinutes(30), Duration.ofMinutes(30)));

    assertThatThrownBy(() -> provider.create("session-id-1", Map.of(), Duration.ofSeconds(-1)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("ttl");
  }
}
