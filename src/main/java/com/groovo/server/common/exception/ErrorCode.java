package com.groovo.server.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "잘못된 요청입니다."),
  DUPLICATED_VALUE(HttpStatus.CONFLICT, "DUPLICATED_VALUE", "이미 사용 중인 값입니다."),
  DUPLICATED_EMAIL(HttpStatus.CONFLICT, "DUPLICATED_VALUE", "이미 사용 중인 이메일입니다."),
  DUPLICATED_NICKNAME(HttpStatus.CONFLICT, "DUPLICATED_VALUE", "이미 사용 중인 닉네임입니다."),
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "리소스를 찾을 수 없습니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
  VIDEO_NOT_FOUND(HttpStatus.NOT_FOUND, "VIDEO_NOT_FOUND", "존재하지 않는 영상입니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 사용자입니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
