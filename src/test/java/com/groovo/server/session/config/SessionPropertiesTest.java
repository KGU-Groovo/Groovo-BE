package com.groovo.server.session.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class SessionPropertiesTest {
	@Test
	void constructor_acceptsWsUrl() {
		SessionProperties properties = new SessionProperties("wss://ai.test/ws/analyze");

		assertThat(properties.wsUrl()).isEqualTo("wss://ai.test/ws/analyze");
	}

	@Test
	void constructor_rejectsBlankWsUrl() {
		assertThatThrownBy(() -> new SessionProperties(" "))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("app.session.ws-url");
	}

	@Test
	void constructor_rejectsNonWsUrl() {
		assertThatThrownBy(() -> new SessionProperties("https://ai.test/ws/analyze"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("ws:// or wss://");
	}
}
