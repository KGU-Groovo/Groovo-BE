package com.groovo.server.common.response;

import java.time.Instant;

public record ApiResponse<T>(
	boolean success,
	String code,
	String message,
	T data,
	Instant timestamp
) {

	private static final String SUCCESS_CODE = "SUCCESS";
	private static final String SUCCESS_MESSAGE = "요청이 성공적으로 처리되었습니다.";

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(true, SUCCESS_CODE, SUCCESS_MESSAGE, data, Instant.now());
	}

	public static ApiResponse<Void> empty() {
		return success(null);
	}

	public static <T> ApiResponse<T> failure(String code, String message) {
		return new ApiResponse<>(false, code, message, null, Instant.now());
	}
}
