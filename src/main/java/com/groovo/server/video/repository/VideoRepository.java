package com.groovo.server.video.repository;

import com.groovo.server.video.domain.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {}
