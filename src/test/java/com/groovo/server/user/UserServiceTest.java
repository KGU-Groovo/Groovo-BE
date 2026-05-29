package com.groovo.server.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.groovo.server.common.exception.BusinessException;
import com.groovo.server.common.exception.ErrorCode;
import com.groovo.server.user.domain.Provider;
import com.groovo.server.user.domain.User;
import com.groovo.server.user.dto.UserResponse;
import com.groovo.server.user.repository.UserRepository;
import com.groovo.server.user.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User sampleUser(Long id) {
		return User.builder()
			.email("user" + id + "@example.com")
			.nickname("댄서" + id)
			.profileImageUrl("https://cdn.groovo.io/profile/" + id + ".jpg")
			.provider(Provider.KAKAO)
			.providerId("kakao-" + id)
			.build();
	}

	@Test
	void getUser_returnsResponse_whenExists() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser(1L)));

		UserResponse response = userService.getUser(1L);

		assertThat(response.email()).isEqualTo("user1@example.com");
		assertThat(response.nickname()).isEqualTo("댄서1");
		assertThat(response.provider()).isEqualTo(Provider.KAKAO);
	}

	@Test
	void getUser_throwsUserNotFound_whenMissing() {
		when(userRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getUser(999L))
			.isInstanceOf(BusinessException.class)
			.satisfies(ex -> assertThat(((BusinessException) ex).errorCode())
				.isEqualTo(ErrorCode.USER_NOT_FOUND));
	}
}
