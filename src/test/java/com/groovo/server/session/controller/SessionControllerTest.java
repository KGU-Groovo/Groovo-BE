package com.groovo.server.session.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.groovo.server.session.repository.SessionRedisStore;
import com.groovo.server.session.repository.SessionRepository;
import com.groovo.server.video.domain.Video;
import com.groovo.server.video.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SessionControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private VideoRepository videoRepository;

	@Autowired
	private SessionRepository sessionRepository;

	@MockitoBean
	private SessionRedisStore sessionRedisStore;

	private Long videoId;

	@BeforeEach
	void setUp() {
		sessionRepository.deleteAll();
		videoRepository.deleteAll();
		Video video = videoRepository.save(Video.builder()
			.title("K-POP 기초 안무 1")
			.videoUrl("https://cdn.groovo.io/video/1/index.m3u8")
			.keypointPath("keypoints/video_1.npy")
			.fps(30.0)
			.durationMs(180000)
			.thumbnailUrl("https://cdn.groovo.io/thumb/1.jpg")
			.build());
		videoId = video.getId();
	}

	@Test
	void createSession_returns201WithWsToken() throws Exception {
		mockMvc.perform(post("/v1/sessions")
				.header("X-User-Id", "101")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"video_id\": " + videoId + "}"))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.session_id").exists())
			.andExpect(jsonPath("$.data.ws_token").exists())
			.andExpect(jsonPath("$.data.ws_url").value("wss://test.groovo.io/ws/analyze"))
			.andExpect(jsonPath("$.data.expires_in").value(1800));
	}

	@Test
	void createSession_returns401_whenUserIdHeaderMissing() throws Exception {
		mockMvc.perform(post("/v1/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"video_id\": " + videoId + "}"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void createSession_returns401_whenUserIdHeaderNotNumeric() throws Exception {
		mockMvc.perform(post("/v1/sessions")
				.header("X-User-Id", "abc")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"video_id\": " + videoId + "}"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void createSession_returns400_whenVideoIdMissing() throws Exception {
		mockMvc.perform(post("/v1/sessions")
				.header("X-User-Id", "101")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"));
	}

	@Test
	void createSession_returns404_whenVideoMissing() throws Exception {
		mockMvc.perform(post("/v1/sessions")
				.header("X-User-Id", "101")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"video_id\": 999999}"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("VIDEO_NOT_FOUND"));
	}
}
