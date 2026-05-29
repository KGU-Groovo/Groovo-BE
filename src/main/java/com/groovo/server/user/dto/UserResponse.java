package com.groovo.server.user.dto;

import com.groovo.server.user.domain.Provider;
import com.groovo.server.user.domain.Role;
import com.groovo.server.user.domain.User;
import com.groovo.server.user.domain.UserStatus;
import java.time.LocalDateTime;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserResponse(
	Long userId,
	String email,
	String nickname,
	String profileImageUrl,
	Provider provider,
	Role role,
	UserStatus status,
	LocalDateTime createdAt
) {

	public static UserResponse from(User user) {
		return new UserResponse(
			user.getId(),
			user.getEmail(),
			user.getNickname(),
			user.getProfileImageUrl(),
			user.getProvider(),
			user.getRole(),
			user.getStatus(),
			user.getCreatedAt()
		);
	}
}
