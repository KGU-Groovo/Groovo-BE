package com.groovo.server.session.controller;

import com.groovo.server.session.dto.SessionCreateRequest;
import com.groovo.server.session.dto.SessionCreateResponse;
import com.groovo.server.session.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/sessions")
public class SessionController {
	private final SessionService sessionService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SessionCreateResponse create(
		@AuthenticationPrincipal Long userId,
		@Valid @RequestBody SessionCreateRequest request
	) {
		return sessionService.create(userId, request.videoId());
	}
}
