package com.groovo.server.session.repository;

import java.time.Duration;
import java.util.Map;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class SessionRedisStore {
  private static final String KEY_PREFIX = "session:";

  private final RedisTemplate<String, String> redisTemplate;

  public SessionRedisStore(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public void save(String sessionId, Map<String, String> fields, Duration ttl) {
    String key = KEY_PREFIX + sessionId;
    redisTemplate.<String, String>opsForHash().putAll(key, fields);
    redisTemplate.expire(key, ttl);
  }
}
