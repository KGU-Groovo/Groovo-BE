package com.groovo.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

  @Bean
  RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(StringRedisSerializer.UTF_8);
    template.setValueSerializer(StringRedisSerializer.UTF_8);
    template.setHashKeySerializer(StringRedisSerializer.UTF_8);
    template.setHashValueSerializer(StringRedisSerializer.UTF_8);
    template.afterPropertiesSet();
    return template;
  }
}
