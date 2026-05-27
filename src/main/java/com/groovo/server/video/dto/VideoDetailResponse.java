package com.groovo.server.video.dto;

import com.groovo.server.video.Video;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VideoDetailResponse(
	Long videoId,
	String title,
	String description,
	String videoUrl,
	Integer durationMs,
	Double fps,
	String thumbnailUrl
) {

	public static VideoDetailResponse from(Video video) {
		return new VideoDetailResponse(
			video.getId(),
			video.getTitle(),
			video.getDescription(),
			video.getVideoUrl(),
			video.getDurationMs(),
			video.getFps(),
			video.getThumbnailUrl()
		);
	}
}
