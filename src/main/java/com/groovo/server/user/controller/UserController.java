package com.groovo.server.user.controller;

import com.groovo.server.user.dto.UserProfileResponse;
import com.groovo.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
public class UserController {

	private final UserService userService;

	// 다른 사용자의 공개 프로필 조회: 닉네임/프로필 이미지 등 비민감 정보만 반환한다.
	// email 등 본인 전체 정보는 auth(JWT) 구현 후 GET /v1/users/me 로 제공 예정.
	@GetMapping("/{userId}")
	public UserProfileResponse getUserProfile(@PathVariable Long userId) {
		return userService.getUserProfile(userId);
	}
}
