package com.groovo.server.video.dto;

import com.groovo.server.video.domain.Video;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record VideoSummaryResponse(
	Long videoId,
	String title,
	String thumbnailUrl,
	Integer durationMs
) {

	public static VideoSummaryResponse from(Video video) {
		return new VideoSummaryResponse(
			video.getId(),
			video.getTitle(),
			video.getThumbnailUrl(),
			video.getDurationMs()
		);
	}
}
