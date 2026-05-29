package com.groovo.server.session.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.groovo.server.common.exception.BusinessException;
import com.groovo.server.common.exception.ErrorCode;
import com.groovo.server.common.security.JwtProperties;
import com.groovo.server.common.security.JwtProvider;
import com.groovo.server.session.config.SessionProperties;
import com.groovo.server.session.domain.Session;
import com.groovo.server.session.domain.SessionStatus;
import com.groovo.server.session.dto.SessionCreateResponse;
import com.groovo.server.session.repository.SessionRedisStore;
import com.groovo.server.session.repository.SessionRepository;
import com.groovo.server.video.domain.Video;
import com.groovo.server.video.repository.VideoRepository;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
	private static final String SECRET = "test-secret-key-for-jwt-signing-0123456789";

	@Mock
	private VideoRepository videoRepository;

	@Mock
	private SessionRepository sessionRepository;

	@Mock
	private SessionRedisStore sessionRedisStore;

	@Mock
	private JwtProvider jwtProvider;

	private SessionService sessionService;

	@BeforeEach
	void setUp() {
		sessionService = new SessionService(
			videoRepository,
			sessionRepository,
			sessionRedisStore,
			jwtProvider,
			new JwtProperties(SECRET, Duration.ofMinutes(30)),
			new SessionProperties("wss://ai.test/ws/analyze")
		);
	}

	@Test
	void create_throwsVideoNotFound_whenVideoMissing() {
		when(videoRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> sessionService.create(101L, 999L))
			.isInstanceOfSatisfying(BusinessException.class, exception ->
				assertThat(exception.errorCode()).isEqualTo(ErrorCode.VIDEO_NOT_FOUND));
	}

	@Test
	@SuppressWarnings("unchecked")
	void create_savesSessionAndRedis_andReturnsResponse() {
		Video video = Video.builder()
			.title("Video 1")
			.description("description")
			.videoUrl("https://cdn.test/video_1.mp4")
			.keypointPath("keypoints/video_1.npy")
			.fps(30.0)
			.durationMs(120_000)
			.thumbnailUrl("https://cdn.test/video_1.jpg")
			.build();
		ReflectionTestUtils.setField(video, "id", 42L);
		when(videoRepository.findById(42L)).thenReturn(Optional.of(video));
		when(jwtProvider.create(any(String.class), any(Map.class), eq(Duration.ofMinutes(30))))
			.thenReturn("ws-token-value");

		SessionCreateResponse response = sessionService.create(101L, 42L);

		assertThat(response.sessionId()).isNotBlank();
		assertThat(response.wsToken()).isEqualTo("ws-token-value");
		assertThat(response.wsUrl()).isEqualTo("wss://ai.test/ws/analyze");
		assertThat(response.expiresIn()).isEqualTo(1800);

		ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
		verify(sessionRepository).save(sessionCaptor.capture());
		Session savedSession = sessionCaptor.getValue();
		assertThat(savedSession.getId()).isEqualTo(response.sessionId());
		assertThat(savedSession.getUserId()).isEqualTo(101L);
		assertThat(savedSession.getVideo()).isSameAs(video);
		assertThat(savedSession.getStatus()).isEqualTo(SessionStatus.ACTIVE);
		assertThat(savedSession.getStartedAt()).isNotNull();

		ArgumentCaptor<Map<String, String>> fieldsCaptor = ArgumentCaptor.forClass(Map.class);
		verify(sessionRedisStore).save(eq(response.sessionId()), fieldsCaptor.capture(), eq(Duration.ofMinutes(30)));
		assertThat(fieldsCaptor.getValue())
			.containsEntry("user_id", "101")
			.containsEntry("video_id", "42")
			.containsEntry("keypoint_path", "keypoints/video_1.npy")
			.containsEntry("status", "active");
	}
}
