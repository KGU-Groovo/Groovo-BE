package com.groovo.server.auth.controller;

import com.groovo.server.auth.dto.LoginRequest;
import com.groovo.server.auth.dto.LogoutResponse;
import com.groovo.server.auth.dto.SignupRequest;
import com.groovo.server.auth.dto.SignupResponse;
import com.groovo.server.auth.dto.TokenResponse;
import com.groovo.server.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public LogoutResponse logout(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        authService.logout(token);
        return new LogoutResponse();
    }
}
