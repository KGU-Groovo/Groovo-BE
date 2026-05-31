package com.groovo.server.session.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.session")
public record SessionProperties(String wsUrl) {
  public SessionProperties {
    if (wsUrl == null || wsUrl.isBlank()) {
      throw new IllegalArgumentException("app.session.ws-url must not be blank");
    }
    if (!wsUrl.startsWith("ws://") && !wsUrl.startsWith("wss://")) {
      throw new IllegalArgumentException("app.session.ws-url must start with ws:// or wss://");
    }
  }
}
