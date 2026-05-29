package com.groovo.server.user.controller;

import com.groovo.server.user.dto.UserResponse;
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

	// 임시 엔드포인트: auth(JWT) 구현 완료 시 GET /v1/users/me 로 대체/보완 예정
	@GetMapping("/{userId}")
	public UserResponse getUser(@PathVariable Long userId) {
		return userService.getUser(userId);
	}
}
