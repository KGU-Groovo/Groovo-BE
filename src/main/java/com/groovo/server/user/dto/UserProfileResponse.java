package com.groovo.server.user.dto;

import com.groovo.server.user.domain.User;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserProfileResponse(Long userId, String nickname, String profileImageUrl) {

  public static UserProfileResponse from(User user) {
    return new UserProfileResponse(user.getId(), user.getNickname(), user.getProfileImageUrl());
  }
}
