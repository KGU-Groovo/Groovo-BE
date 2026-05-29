package com.groovo.server.common.exception;

import com.groovo.server.common.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception) {
		ErrorCode errorCode = exception.errorCode();
		return ResponseEntity
			.status(errorCode.status())
			.body(ApiResponse.failure(errorCode.code(), exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException exception
	) {
		String message = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> error.getField() + ": " + error.getDefaultMessage())
			.collect(Collectors.joining(", "));
		return invalidInput(message);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(
		ConstraintViolationException exception
	) {
		String message = exception.getConstraintViolations()
			.stream()
			.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
			.collect(Collectors.joining(", "));
		return invalidInput(message);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException exception
	) {
		return invalidInput(ErrorCode.INVALID_INPUT_VALUE.message());
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse<Void>> handleAuthenticationException() {
		return error(ErrorCode.UNAUTHORIZED);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException() {
		return error(ErrorCode.FORBIDDEN);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(Exception exception) {
		log.error("Unhandled exception occurred.", exception);
		return error(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<ApiResponse<Void>> invalidInput(String message) {
		ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
		String responseMessage = message == null || message.isBlank() ? errorCode.message() : message;
		return ResponseEntity
			.status(errorCode.status())
			.body(ApiResponse.failure(errorCode.code(), responseMessage));
	}

	private ResponseEntity<ApiResponse<Void>> error(ErrorCode errorCode) {
		return ResponseEntity
			.status(errorCode.status())
			.body(ApiResponse.failure(errorCode.code(), errorCode.message()));
	}
}
