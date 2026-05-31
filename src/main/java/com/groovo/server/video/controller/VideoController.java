package com.groovo.server.video.controller;

import com.groovo.server.video.dto.VideoDetailResponse;
import com.groovo.server.video.dto.VideoListResponse;
import com.groovo.server.video.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/videos")
public class VideoController {

	private final VideoService videoService;

	@GetMapping
	public VideoListResponse getVideos(
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "20") int size
	) {
		Pageable pageable = PageRequest.of(page, size);
		return videoService.getVideos(pageable);
	}

	@GetMapping("/{videoId}")
	public VideoDetailResponse getVideo(@PathVariable Long videoId) {
		return videoService.getVideo(videoId);
	}
}
