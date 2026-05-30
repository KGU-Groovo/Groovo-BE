package com.groovo.server.auth.dto;

import lombok.Getter;

@Getter
public class LogoutResponse {
    private final String message = "로그아웃되었습니다.";
}