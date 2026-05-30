package com.groovo.server.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class SignupResponse {
    @JsonProperty("user_id")
    private Long userId;
    private String email;
    private String nickname;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}