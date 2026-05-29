package com.groovo.server.session.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.groovo.server.session.domain.Session;
import com.groovo.server.session.domain.SessionStatus;
import com.groovo.server.session.repository.SessionRedisStore;
import com.groovo.server.session.repository.SessionRepository;
import com.groovo.server.video.domain.Video;
import com.groovo.server.video.repository.VideoRepository;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
	private static final String KEYPOINT_PATH = "keypoints/video_1.npy";

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
			.keypointPath(KEYPOINT_PATH)
			.fps(30.0)
			.durationMs(180000)
			.thumbnailUrl("https://cdn.groovo.io/thumb/1.jpg")
			.build());
		videoId = video.getId();
	}

	@Test
	@SuppressWarnings("unchecked")
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

		List<Session> sessions = sessionRepository.findAll();
		assertThat(sessions).hasSize(1);
		Session session = sessions.get(0);
		assertThat(session.getUserId()).isEqualTo(101L);
		assertThat(session.getVideo().getId()).isEqualTo(videoId);
		assertThat(session.getStatus()).isEqualTo(SessionStatus.ACTIVE);
		assertThat(session.getStartedAt()).isNotNull();
		assertThat(session.getFinishedAt()).isNull();

		ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<Map<String, String>> fieldsCaptor = ArgumentCaptor.forClass(Map.class);
		verify(sessionRedisStore).save(sessionIdCaptor.capture(), fieldsCaptor.capture(), eq(Duration.ofMinutes(30)));
		assertThat(sessionIdCaptor.getValue()).isEqualTo(session.getId());
		assertThat(fieldsCaptor.getValue())
			.containsEntry("user_id", "101")
			.containsEntry("video_id", String.valueOf(videoId))
			.containsEntry("keypoint_path", KEYPOINT_PATH)
			.containsEntry("fps", "30.0")
			.containsEntry("status", "active");
		assertThat(fieldsCaptor.getValue().get("started_at"))
			.isNotBlank()
			.containsOnlyDigits();
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
	void createSession_returns400_whenVideoIdHasWrongType() throws Exception {
		mockMvc.perform(post("/v1/sessions")
				.header("X-User-Id", "101")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"video_id\": \"abc\"}"))
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
