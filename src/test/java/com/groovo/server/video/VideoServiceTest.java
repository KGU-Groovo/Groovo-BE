package com.groovo.server.video;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.groovo.server.common.exception.BusinessException;
import com.groovo.server.common.exception.ErrorCode;
import com.groovo.server.video.dto.VideoDetailResponse;
import com.groovo.server.video.dto.VideoListResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

	@Mock
	private VideoRepository videoRepository;

	@InjectMocks
	private VideoService videoService;

	private Video sampleVideo(Long id) {
		return Video.builder()
			.title("title-" + id)
			.description("desc-" + id)
			.videoUrl("https://cdn.groovo.io/video/" + id + "/index.m3u8")
			.keypointPath("keypoints/video_" + id + ".npy")
			.fps(30.0)
			.durationMs(180000)
			.thumbnailUrl("https://cdn.groovo.io/thumb/" + id + ".jpg")
			.build();
	}

	@Test
	void getVideos_mapsContentAndFillsPagingMeta() {
		Pageable pageable = PageRequest.of(0, 2);
		List<Video> videos = List.of(sampleVideo(1L), sampleVideo(2L));
		when(videoRepository.findAll(pageable)).thenReturn(new PageImpl<>(videos, pageable, 5));

		VideoListResponse response = videoService.getVideos(pageable);

		assertThat(response.videos()).hasSize(2);
		assertThat(response.page()).isEqualTo(0);
		assertThat(response.size()).isEqualTo(2);
		assertThat(response.totalElements()).isEqualTo(5);
		assertThat(response.totalPages()).isEqualTo(3);
	}

	@Test
	void getVideo_returnsDetail_whenExists() {
		when(videoRepository.findById(1L)).thenReturn(Optional.of(sampleVideo(1L)));

		VideoDetailResponse response = videoService.getVideo(1L);

		assertThat(response.videoUrl()).isEqualTo("https://cdn.groovo.io/video/1/index.m3u8");
		assertThat(response.fps()).isEqualTo(30.0);
	}

	@Test
	void getVideo_throwsVideoNotFound_whenMissing() {
		when(videoRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> videoService.getVideo(999L))
			.isInstanceOf(BusinessException.class)
			.satisfies(ex -> assertThat(((BusinessException) ex).errorCode())
				.isEqualTo(ErrorCode.VIDEO_NOT_FOUND));
	}
}
