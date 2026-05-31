package com.groovo.server.config;

import com.groovo.server.common.jwt.JwtProperties;
import com.groovo.server.session.config.SessionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, SessionProperties.class})
public class JwtConfig {}
