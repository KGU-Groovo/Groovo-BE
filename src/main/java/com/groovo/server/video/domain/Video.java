package com.groovo.server.video.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "videos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Video {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "video_url", nullable = false)
  private String videoUrl;

  @Column(name = "keypoint_path")
  private String keypointPath;

  private Double fps;

  @Column(name = "duration_ms")
  private Integer durationMs;

  @Column(name = "thumbnail_url")
  private String thumbnailUrl;

  @Builder
  private Video(
      String title,
      String description,
      String videoUrl,
      String keypointPath,
      Double fps,
      Integer durationMs,
      String thumbnailUrl) {
    this.title = title;
    this.description = description;
    this.videoUrl = videoUrl;
    this.keypointPath = keypointPath;
    this.fps = fps;
    this.durationMs = durationMs;
    this.thumbnailUrl = thumbnailUrl;
  }
}
