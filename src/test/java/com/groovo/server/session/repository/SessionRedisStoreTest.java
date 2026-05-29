package com.groovo.server.session.repository;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class SessionRedisStoreTest {
	@Mock
	private RedisTemplate<String, String> redisTemplate;

	@Mock
	private HashOperations<String, String, String> hashOperations;

	@InjectMocks
	private SessionRedisStore sessionRedisStore;

	@Test
	void save_writesHashAndSetsTtl() {
		when(redisTemplate.<String, String>opsForHash()).thenReturn(hashOperations);
		Map<String, String> fields = Map.of("status", "active", "user_id", "101");
		Duration ttl = Duration.ofMinutes(30);

		sessionRedisStore.save("sid-1", fields, ttl);

		verify(hashOperations).putAll("session:sid-1", fields);
		verify(redisTemplate).expire("session:sid-1", ttl);
	}
}
