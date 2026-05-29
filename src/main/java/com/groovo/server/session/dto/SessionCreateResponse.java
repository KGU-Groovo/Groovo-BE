package com.groovo.server.session.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SessionCreateResponse(
	String sessionId,
	String wsToken,
	String wsUrl,
	long expiresIn
) {
}
