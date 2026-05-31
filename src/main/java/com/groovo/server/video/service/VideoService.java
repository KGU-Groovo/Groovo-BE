package com.groovo.server.video.service;

import com.groovo.server.common.exception.BusinessException;
import com.groovo.server.common.exception.ErrorCode;
import com.groovo.server.video.domain.Video;
import com.groovo.server.video.dto.VideoDetailResponse;
import com.groovo.server.video.dto.VideoListResponse;
import com.groovo.server.video.dto.VideoSummaryResponse;
import com.groovo.server.video.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoService {

  private final VideoRepository videoRepository;

  public VideoListResponse getVideos(Pageable pageable) {
    Page<VideoSummaryResponse> page =
        videoRepository.findAll(pageable).map(VideoSummaryResponse::from);
    return VideoListResponse.from(page);
  }

  public VideoDetailResponse getVideo(Long videoId) {
    Video video =
        videoRepository
            .findById(videoId)
            .orElseThrow(() -> new BusinessException(ErrorCode.VIDEO_NOT_FOUND));
    return VideoDetailResponse.from(video);
  }
}
