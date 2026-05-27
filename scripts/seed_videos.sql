-- 데모용 레퍼런스 영상 시드. 멱등(ON DUPLICATE KEY UPDATE)하므로 재실행해도 안전합니다.
-- videos 테이블은 앱 기동 시 Hibernate(ddl-auto: update)가 생성하므로,
-- 애플리케이션을 한 번 기동한 뒤 실행하세요.
INSERT INTO videos (id, title, description, video_url, keypoint_path, fps, duration_ms, thumbnail_url)
VALUES
  (1, 'K-POP 기초 안무 1', '기초 동작 연습용', 'https://cdn.groovo.io/video/1/index.m3u8', 'keypoints/video_1.npy', 30.0, 180000, 'https://cdn.groovo.io/thumb/1.jpg'),
  (2, 'K-POP 기초 안무 2', '심화 동작 연습용', 'https://cdn.groovo.io/video/2/index.m3u8', 'keypoints/video_2.npy', 30.0, 210000, 'https://cdn.groovo.io/thumb/2.jpg'),
  (3, 'K-POP 댄스 챌린지', '최신 챌린지 안무', 'https://cdn.groovo.io/video/3/index.m3u8', 'keypoints/video_3.npy', 30.0, 95000, 'https://cdn.groovo.io/thumb/3.jpg')
ON DUPLICATE KEY UPDATE
  title = VALUES(title),
  description = VALUES(description),
  video_url = VALUES(video_url),
  keypoint_path = VALUES(keypoint_path),
  fps = VALUES(fps),
  duration_ms = VALUES(duration_ms),
  thumbnail_url = VALUES(thumbnail_url);
