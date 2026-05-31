package com.groovo.server.session.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.groovo.server.session.domain.Session;
import com.groovo.server.session.domain.SessionStatus;
import com.groovo.server.user.domain.User;
import com.groovo.server.user.repository.UserRepository;
import com.groovo.server.video.domain.Video;
import com.groovo.server.video.repository.VideoRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SessionRepositoryTest {
	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private VideoRepository videoRepository;

	@Test
	void save_persistsSessionWithVideoFkAndStatus() {
		User user = userRepository.save(User.builder()
			.email("session-user@test.com")
			.password("encoded-password")
			.nickname("session-user")
			.build());
		Video video = videoRepository.save(Video.builder()
			.title("ref-1")
			.videoUrl("https://cdn.groovo.io/video/1/index.m3u8")
			.keypointPath("keypoints/video_1.npy")
			.fps(30.0)
			.durationMs(180000)
			.thumbnailUrl("https://cdn.groovo.io/thumb/1.jpg")
			.build());

		sessionRepository.save(Session.builder()
			.id("sid-1")
			.user(user)
			.video(video)
			.status(SessionStatus.ACTIVE)
			.startedAt(Instant.now())
			.build());

		Session found = sessionRepository.findById("sid-1").orElseThrow();
		assertThat(found.getUser().getId()).isEqualTo(user.getId());
		assertThat(found.getVideo().getId()).isEqualTo(video.getId());
		assertThat(found.getStatus()).isEqualTo(SessionStatus.ACTIVE);
		assertThat(found.getFinishedAt()).isNull();
	}
}
