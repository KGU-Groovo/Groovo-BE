package com.groovo.server.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.groovo.server.common.exception.BusinessException;
import com.groovo.server.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.NativeWebRequest;

@ExtendWith(MockitoExtension.class)
class CurrentUserIdArgumentResolverTest {
	@Mock
	private NativeWebRequest webRequest;

	private final CurrentUserIdArgumentResolver resolver = new CurrentUserIdArgumentResolver();

	@Test
	void resolveArgument_returnsUserId_whenHeaderIsNumeric() throws Exception {
		when(webRequest.getHeader("X-User-Id")).thenReturn("101");

		Object result = resolver.resolveArgument(null, null, webRequest, null);

		assertThat(result).isEqualTo(101L);
	}

	@Test
	void resolveArgument_throwsUnauthorized_whenHeaderMissing() {
		when(webRequest.getHeader("X-User-Id")).thenReturn(null);

		assertThatThrownBy(() -> resolver.resolveArgument(null, null, webRequest, null))
			.isInstanceOf(BusinessException.class)
			.satisfies(ex -> assertThat(((BusinessException) ex).errorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
	}

	@Test
	void resolveArgument_throwsUnauthorized_whenHeaderNotNumeric() {
		when(webRequest.getHeader("X-User-Id")).thenReturn("abc");

		assertThatThrownBy(() -> resolver.resolveArgument(null, null, webRequest, null))
			.isInstanceOf(BusinessException.class)
			.satisfies(ex -> assertThat(((BusinessException) ex).errorCode()).isEqualTo(ErrorCode.UNAUTHORIZED));
	}
}
