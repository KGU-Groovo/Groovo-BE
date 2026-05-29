package com.groovo.server.session.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.session")
public record SessionProperties(
	String wsUrl
) {
}
