package com.groovo.server.video;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.groovo.server.common.jwt.JwtProvider;
import com.groovo.server.user.domain.User;
import com.groovo.server.user.repository.UserRepository;
import com.groovo.server.video.domain.Video;
import com.groovo.server.video.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VideoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtProvider jwtProvider;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private VideoRepository videoRepository;

	private Long firstVideoId;
	private String accessToken;

	@BeforeEach
	void setUp() {
		videoRepository.deleteAll();
		userRepository.deleteAll();
		User user = userRepository.save(User.builder()
			.email("video-viewer@test.com")
			.password("encoded-password")
			.nickname("video-viewer")
			.build());
		accessToken = jwtProvider.generateToken(user.getId());
		Video first = videoRepository.save(Video.builder()
			.title("K-POP 기초 안무 1")
			.description("기초 동작 연습용")
			.videoUrl("https://cdn.groovo.io/video/1/index.m3u8")
			.keypointPath("keypoints/video_1.npy")
			.fps(30.0)
			.durationMs(180000)
			.thumbnailUrl("https://cdn.groovo.io/thumb/1.jpg")
			.build());
		videoRepository.save(Video.builder()
			.title("K-POP 기초 안무 2")
			.description("심화 동작 연습용")
			.videoUrl("https://cdn.groovo.io/video/2/index.m3u8")
			.keypointPath("keypoints/video_2.npy")
			.fps(30.0)
			.durationMs(210000)
			.thumbnailUrl("https://cdn.groovo.io/thumb/2.jpg")
			.build());
		firstVideoId = first.getId();
	}

	@Test
	void getVideos_returnsPagedListWithSnakeCaseAndMeta() throws Exception {
		mockMvc.perform(get("/v1/videos")
				.header("Authorization", bearerToken())
				.param("page", "0")
				.param("size", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.videos.length()").value(1))
			.andExpect(jsonPath("$.data.videos[0].video_id").exists())
			.andExpect(jsonPath("$.data.videos[0].thumbnail_url").exists())
			.andExpect(jsonPath("$.data.videos[0].duration_ms").exists())
			.andExpect(jsonPath("$.data.page").value(0))
			.andExpect(jsonPath("$.data.size").value(1))
			.andExpect(jsonPath("$.data.total_elements").value(2))
			.andExpect(jsonPath("$.data.total_pages").value(2));
	}

	@Test
	void getVideo_returnsDetailWithoutKeypointPath() throws Exception {
		mockMvc.perform(get("/v1/videos/{videoId}", firstVideoId)
				.header("Authorization", bearerToken()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.video_id").value(firstVideoId))
			.andExpect(jsonPath("$.data.video_url").exists())
			.andExpect(jsonPath("$.data.fps").value(30.0))
			.andExpect(jsonPath("$.data.description").exists())
			.andExpect(jsonPath("$.data.keypoint_path").doesNotExist());
	}

	@Test
	void getVideo_returns404_whenMissing() throws Exception {
		mockMvc.perform(get("/v1/videos/{videoId}", 999999)
				.header("Authorization", bearerToken()))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.success").value(false))
			.andExpect(jsonPath("$.code").value("VIDEO_NOT_FOUND"));
	}

	@Test
	void getVideos_returns401_whenAuthorizationHeaderMissing() throws Exception {
		mockMvc.perform(get("/v1/videos"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	private String bearerToken() {
		return "Bearer " + accessToken;
	}
}
