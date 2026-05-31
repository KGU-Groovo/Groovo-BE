package com.groovo.server.infra.redis;

import java.time.Duration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisQueueConsumer {

	private final RedisTemplate<String, String> redisTemplate;

	public RedisQueueConsumer(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public String consume(String queueName, Duration timeout) {
		return redisTemplate.opsForList().rightPop(queueName, timeout);
	}
}
