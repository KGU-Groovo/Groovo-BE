package com.groovo.server.infra.redis;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisQueuePublisher {

  private final RedisTemplate<String, String> redisTemplate;

  public RedisQueuePublisher(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  public void publish(String queueName, String payload) {
    redisTemplate.opsForList().leftPush(queueName, payload);
  }
}
