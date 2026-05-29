package com.groovo.server.user;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.groovo.server.user.domain.Provider;
import com.groovo.server.user.domain.User;
import com.groovo.server.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	private Long userId;

	@BeforeEach
	void setUp() {
		userRepository.deleteAll();
		User user = userRepository.save(User.builder()
			.email("dancer@example.com")
			.nickname("댄서A")
			.profileImageUrl("https://cdn.groovo.io/profile/1.jpg")
			.provider(Provider.KAKAO)
			.providerId("kakao-1")
			.build());
		userId = user.getId();
	}

	@Test
	void getUser_returnsProfileWithoutProviderId() throws Exception {
		mockMvc.perform(get("/v1/users/{userId}", userId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.user_id").value(userId))
			.andExpect(jsonPath("$.data.email").value("dancer@example.com"))
			.andExpect(jsonPath("$.data.nickname").value("댄서A"))
			.andExpect(jsonPath("$.data.provider").value("KAKAO"))
			.andExpect(jsonPath("$.data.role").value("USER"))
			.andExpect(jsonPath("$.data.status").value("ACTIVE"))
			.andExpect(jsonPath("$.data.created_at").exists())
			.andExpect(jsonPath("$.data.provider_id").doesNotExist());
	}

	@Test
	void getUser_returns404_whenMissing() throws Exception {
		mockMvc.perform(get("/v1/users/{userId}", 999999))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
	}
}
