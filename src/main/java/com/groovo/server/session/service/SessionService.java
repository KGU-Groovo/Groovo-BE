package com.groovo.server.session.service;

import com.groovo.server.common.exception.BusinessException;
import com.groovo.server.common.exception.ErrorCode;
import com.groovo.server.common.jwt.JwtProperties;
import com.groovo.server.common.jwt.JwtProvider;
import com.groovo.server.session.config.SessionProperties;
import com.groovo.server.session.domain.Session;
import com.groovo.server.session.domain.SessionStatus;
import com.groovo.server.session.dto.SessionCreateResponse;
import com.groovo.server.session.repository.SessionRedisStore;
import com.groovo.server.session.repository.SessionRepository;
import com.groovo.server.user.domain.User;
import com.groovo.server.user.repository.UserRepository;
import com.groovo.server.video.domain.Video;
import com.groovo.server.video.repository.VideoRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {
	private final UserRepository userRepository;
	private final VideoRepository videoRepository;
	private final SessionRepository sessionRepository;
	private final SessionRedisStore sessionRedisStore;
	private final JwtProvider jwtProvider;
	private final JwtProperties jwtProperties;
	private final SessionProperties sessionProperties;

	public SessionCreateResponse create(Long userId, Long videoId) {
		if (userId == null) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
		Video video = videoRepository.findById(videoId)
			.orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));
		validateAnalysisMetadata(video);

		String sessionId = UUID.randomUUID().toString();
		Duration ttl = jwtProperties.wsTokenExpiration();

		Map<String, Object> claims = new LinkedHashMap<>();
		claims.put("userId", userId);
		claims.put("videoId", video.getId());
		String wsToken = jwtProvider.create(sessionId, claims, ttl);

		Instant now = Instant.now();
		sessionRepository.save(Session.builder()
			.id(sessionId)
			.user(user)
			.video(video)
			.status(SessionStatus.ACTIVE)
			.startedAt(now)
			.build());

		Map<String, String> fields = new LinkedHashMap<>();
		fields.put("user_id", String.valueOf(user.getId()));
		fields.put("video_id", String.valueOf(video.getId()));
		fields.put("keypoint_path", video.getKeypointPath());
		fields.put("fps", String.valueOf(video.getFps()));
		fields.put("status", "active");
		fields.put("started_at", String.valueOf(now.getEpochSecond()));
		sessionRedisStore.save(sessionId, fields, ttl);

		return new SessionCreateResponse(sessionId, wsToken, sessionProperties.wsUrl(), ttl.toSeconds());
	}

	private void validateAnalysisMetadata(Video video) {
		if (video.getKeypointPath() == null || video.getKeypointPath().isBlank()) {
			throw new BusinessException(
				ErrorCode.INTERNAL_SERVER_ERROR,
				"Video analysis metadata is incomplete: keypointPath is missing"
			);
		}
		if (video.getFps() == null) {
			throw new BusinessException(
				ErrorCode.INTERNAL_SERVER_ERROR,
				"Video analysis metadata is incomplete: fps is missing"
			);
		}
	}
}
