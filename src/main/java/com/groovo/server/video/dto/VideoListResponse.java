package com.groovo.server.video.dto;

import java.util.List;
import org.springframework.data.domain.Page;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VideoListResponse(
	List<VideoSummaryResponse> videos,
	int page,
	int size,
	long totalElements,
	int totalPages
) {

	public static VideoListResponse from(Page<VideoSummaryResponse> page) {
		return new VideoListResponse(
			page.getContent(),
			page.getNumber(),
			page.getSize(),
			page.getTotalElements(),
			page.getTotalPages()
		);
	}
}
