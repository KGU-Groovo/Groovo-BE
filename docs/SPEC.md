# 사용자 관리 (User)

## **내 프로필 조회**

- JWT의 user_id로 본인 정보 조회 (이메일, 닉네임 등)
- 비밀번호 해시는 절대 응답에 포함하지 않음

## **프로필 수정**

- 닉네임 변경 정도
- 데모에서 시간 부족하면 후순위로 미뤄도 됨

# 영상 조회 (Video)

## **영상 목록 조회**

- 연습 가능한 레퍼런스 영상 리스트 (제목, 썸네일, 길이 등)
- 페이징은 데모에선 생략 가능 (영상 몇 개뿐이니까)

## **영상 상세 조회**

- 재생에 필요한 정보 반환 (video_url, duration_ms, fps 등)
- keypoint_path는 클라이언트에 노출 불필요 (AI 서버만 사용)

## 사용자별 기록

해당 엔티에 masteryScore필드도 내가 추가를 했어. 해당 엔티티는 사
용자가 학습을 완료하면, 해당 결과를 기반으로 비디오 목록을 보여줄
때 사용자별 맞춤 정보를 보여주기 위한 정보들이야.

# 세션 관리 (Session)

## **세션 생성**

- JWT 검증 후 user_id 확인, 요청에 담긴 video_id 검증 (존재하는 영상인지)
- session_id(UUID) 생성
- ws_token(JWT) 발급 — session_id를 payload에 포함
- Redis에 세션 정보 기록: `session:{session_id}` → user_id, video_id, status: active
- 응답으로 ws_token과 AI 서버 WebSocket URL 전달

### REDIS ws_token session 키 구조

key: session:{session_id}
value: {
user_id: 123,
video_id: 42,
keypoint_path: "keypoints/video_42.npy",
fps: 30.0,
status: "active", # active / finished
started_at: 1234567890
}
TTL: 30분

## **세션 종료 처리**

- AI 서버로부터 리포트 생성 통지를 받을 때 처리 (리포트와 연동)
- sessions 테이블의 status를 finished로, finished_at 기록

# 리포트 관리 (Report)

## **리포트 생성**

<aside>
💡

리포트 생성 트리거는 AI 서버 주도(WebSocket 종료 시 통지)
데모는 직접 HTTP
MVP 전환 시 Redis Queue + 멱등성/재시도로 변경

</aside>

- AI 서버의 HTTP 통지를 받는 엔드포인트 (요약 점수 + S3 detail 경로 수신)
- reports 테이블에 저장 + 연결된 session status를 finished로 업데이트
- 멱등성 처리: 같은 session_id로 중복 통지 시 중복 생성 방지 (데모에선 간단히, 발전 시 강화 — 문서 메모)

## **리포트 목록 조회**

- JWT의 user_id로 본인 리포트 목록 조회 ("내 연습 기록")
- 영상 제목, 평균 점수, 날짜 등 요약 정보

## **리포트 상세 조회**

- 특정 리포트의 상세 정보 (평균 점수 + S3에서 가져온 프레임별 시계열)
- 본인 리포트만 접근 가능하도록 user_id 검증
