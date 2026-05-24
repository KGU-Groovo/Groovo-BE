package com.groovo.server.common.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

	public static final String REQUEST_ID_HEADER = "X-Request-Id";
	public static final String REQUEST_ID_ATTRIBUTE = "requestId";
	private static final int MAX_BODY_LOG_LENGTH = 4_000;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		long startedAt = System.currentTimeMillis();
		String requestId = resolveRequestId(request);

		response.setHeader(REQUEST_ID_HEADER, requestId);
		request.setAttribute(REQUEST_ID_ATTRIBUTE, requestId);
		CachedBodyRequestWrapper requestWrapper = shouldCacheRequestBody(request)
			? new CachedBodyRequestWrapper(request)
			: null;
		HttpServletRequest requestToUse = requestWrapper == null ? request : requestWrapper;
		ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

		log.info(
			"[REQUEST] requestId={} method={} path={} clientIp={} body={}",
			requestId,
			requestToUse.getMethod(),
			pathWithQuery(requestToUse),
			clientIp(requestToUse),
			requestWrapper == null
				? requestBodyNotLoggedMessage(request)
				: formatBody(requestWrapper.getCachedBody(), requestWrapper.getCharacterEncoding(), requestWrapper.getContentType())
		);

		try {
			filterChain.doFilter(requestToUse, responseWrapper);
		} finally {
			long elapsed = System.currentTimeMillis() - startedAt;
			log.info(
				"[RESPONSE] requestId={} method={} path={} status={} elapsedMs={} body={}",
				requestId,
				requestToUse.getMethod(),
				pathWithQuery(requestToUse),
				responseWrapper.getStatus(),
				elapsed,
				formatBody(
					responseWrapper.getContentAsByteArray(),
					responseWrapper.getCharacterEncoding(),
					responseWrapper.getContentType()
				)
			);
			responseWrapper.copyBodyToResponse();
		}
	}

	private String resolveRequestId(HttpServletRequest request) {
		String requestId = request.getHeader(REQUEST_ID_HEADER);
		if (requestId == null || requestId.isBlank()) {
			return UUID.randomUUID().toString().replace("-", "");
		}
		return requestId.trim();
	}

	private String clientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}

	private String pathWithQuery(HttpServletRequest request) {
		String queryString = request.getQueryString();
		if (queryString == null || queryString.isBlank()) {
			return request.getRequestURI();
		}
		return request.getRequestURI() + "?" + queryString;
	}

	private boolean shouldCacheRequestBody(HttpServletRequest request) {
		String method = request.getMethod();
		if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
			return false;
		}
		return isReadableContent(request.getContentType());
	}

	private String requestBodyNotLoggedMessage(HttpServletRequest request) {
		if (request.getContentLengthLong() <= 0) {
			return "-";
		}
		return "[not logged: " + request.getContentType() + "]";
	}

	private String formatBody(byte[] body, String characterEncoding, String contentType) {
		if (body.length == 0) {
			return "-";
		}
		if (!isReadableContent(contentType)) {
			return "[not logged: " + contentType + "]";
		}

		Charset charset = characterEncoding == null
			? StandardCharsets.UTF_8
			: Charset.forName(characterEncoding);
		String text = new String(body, charset)
			.replace("\r", "\\r")
			.replace("\n", "\\n");
		if (text.length() <= MAX_BODY_LOG_LENGTH) {
			return text;
		}
		return text.substring(0, MAX_BODY_LOG_LENGTH) + "...[truncated]";
	}

	private boolean isReadableContent(String contentType) {
		if (contentType == null) {
			return true;
		}
		String lowerContentType = contentType.toLowerCase();
		return lowerContentType.contains("json")
			|| lowerContentType.contains("text")
			|| lowerContentType.contains("xml");
	}

	private static class CachedBodyRequestWrapper extends HttpServletRequestWrapper {

		private final byte[] cachedBody;

		private CachedBodyRequestWrapper(HttpServletRequest request) throws IOException {
			super(request);
			this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
		}

		private byte[] getCachedBody() {
			return cachedBody;
		}

		@Override
		public ServletInputStream getInputStream() {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(cachedBody);
			return new ServletInputStream() {
				@Override
				public boolean isFinished() {
					return inputStream.available() == 0;
				}

				@Override
				public boolean isReady() {
					return true;
				}

				@Override
				public void setReadListener(ReadListener readListener) {
					throw new UnsupportedOperationException();
				}

				@Override
				public int read() {
					return inputStream.read();
				}
			};
		}

		@Override
		public BufferedReader getReader() {
			Charset charset = getCharacterEncoding() == null
				? StandardCharsets.UTF_8
				: Charset.forName(getCharacterEncoding());
			return new BufferedReader(new InputStreamReader(getInputStream(), charset));
		}
	}
}
