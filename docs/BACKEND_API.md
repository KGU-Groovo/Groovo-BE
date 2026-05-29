## 엔드포인트 요약

| 파트 | 메서드 | 경로 | 인증 |
| --- | --- | --- | --- |
| Auth | POST | /api/v1/auth/signup | ❌ |
| Auth | POST | /api/v1/auth/login | ❌ |
| Auth | POST | /api/v1/auth/logout | ✅ |
| User | GET | /api/v1/users/me | ✅ |
| User | PATCH | /api/v1/users/me | ✅ |
| Video | GET | /api/v1/videos | ✅ |
| Video | GET | /api/v1/videos/{videoId} | ✅ |
| Session | POST | /api/v1/sessions | ✅ |
| Report | POST | /api/v1/internal/reports | 서버 간 인증 |
| Report | GET | /api/v1/reports | ✅ |
| Report | GET | /api/v1/reports/{reportId} | ✅ |

## 공통 사항

**Base URL**

| 환경 | URL |
| --- | --- |
| 로컬 | `http://localhost:8080` |
| 개발 | `https://api-dev.groovo.io` |
| 운영 | `https://api.groovo.io` |

### 인증

인증이 필요한 엔드포인트는 `Authorization` 헤더에 access token(JWT)을 담아 호출합니다.

`Authorization: Bearer <access_token>`

- 데모 버전은 stateless access token만 사용 (refresh token 미구현)
- 발전 방향: refresh token 도입 + 로그아웃 시 refresh token 무효화(Redis)

### 공통 응답 형식

성공 응답은 데이터를 그대로 반환하고, 에러는 아래 형식을 따릅니다.

```json
{
  "code": "ERROR_CODE",
  "message": "사람이 읽을 수 있는 에러 메시지"
}
```

### 공통 HTTP 상태 코드

| 코드 | 의미 |
| --- | --- |
| 200 | 성공 |
| 201 | 생성 성공 |
| 400 | 잘못된 요청 (검증 실패) |
| 401 | 인증 실패 (토큰 없음/만료/위조) |
| 403 | 권한 없음 (본인 리소스 아님) |
| 404 | 리소스 없음 |
| 409 | 충돌 (이메일 중복 등) |

## ① 인증/인가 (Auth)

---

### POST /api/v1/auth/signup

회원가입

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password123!",
  "nickname": "댄서A"
}
```

**Response 201**

```json
{
  "user_id": 101,
  "email": "user@example.com",
  "nickname": "댄서A",
  "created_at": "2026-05-01T10:00:00Z"
}
```

> 비밀번호 해시는 응답에 절대 포함하지 않습니다.
> 

**에러**

| 상태 | code | 상황 |
| --- | --- | --- |
| 400 | BAD_REQUEST | 이메일, 비밀번호, 닉네임 누락 또는 형식 오류 |
| 409 | DUPLICATED_VALUE | 이미 사용 중인 이메일 또는 닉네임 |

---

### POST /api/v1/auth/login

로그인

**Request Body**

```json
{
  "email": "user@example.com",
  "password": "password123!"
}
```

**Response 200**

```json
{
  "access_token": "eyJhbGciOi...",
  "token_type": "Bearer",
  "expires_in": 1800
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| access_token | string | 인증이 필요한 API 호출에 사용할 JWT |
| token_type | string | 토큰 인증 방식 |
| expires_in | int | access_token 만료까지 남은 초 (예: 1800) |

**에러**

| 상태 | code | 상황 |
| --- | --- | --- |
| 400 | BAD_REQUEST | 이메일 또는 비밀번호 누락 |
| 401 | INVALID_CREDENTIALS | 이메일 또는 비밀번호 불일치 |

---

### POST /api/v1/auth/logout

로그아웃

**Request**: Authorization 헤더 필요

**Response 200**

```json
{
  "message": "로그아웃되었습니다."
}
```

**에러**

| 상태 | code | 상황 |
| --- | --- | --- |
| 401 | UNAUTHORIZED | 토큰 없음/만료 |

---

## ② 사용자 (User)

### GET /api/v1/users/me

내 프로필 조회

**Request**: Authorization 헤더 필요

**Response 200**

```json
{
  "user_id": 101,
  "email": "user@example.com",
  "nickname": "댄서A",
  "created_at": "2026-05-01T10:00:00Z"
}
```

> 비밀번호 해시는 응답에 절대 포함하지 않습니다.
> 

**에러**

| 상태 | code | 상황 |
| --- | --- | --- |
| 401 | UNAUTHORIZED | 토큰 없음/만료 |

---

### PATCH /api/v1/users/me (🟡 우선순위 낮음)

프로필 수정 (닉네임). 데모에서 시간 부족 시 후순위.

**Request Body**

```json
{
  "nickname": "새닉네임"
}
```

**Response 200**

```json
{
  "user_id": 101,
  "nickname": "새닉네임"
}
```

---

## ③ 영상 (Video)

### GET /api/v1/videos

레퍼런스 영상 목록 조회

**Request**: Authorization 헤더 필요

**Response 200**

```json
{
  "videos": [
    {
      "video_id": 42,
      "title": "K-POP 기초 안무 1",
      "thumbnail_url": "https://cdn.groovo.io/thumb/42.jpg",
      "duration_ms": 180000
    }
  ]
}
```

> 데모는 영상 수가 적어 페이징 생략 가능.
> 

**에러**

| 상태 | code | 상황 |
| --- | --- | --- |
| 401 | UNAUTHORIZED | 토큰 없음/만료 |

---

### GET /api/v1/videos/{videoId}

영상 상세 조회 (재생에 필요한 정보)

**Request**: Authorization 헤더 필요

**Response 200**

```json
{
  "video_id": 42,
  "title": "K-POP 기초 안무 1",
  "description": "기초 동작 연습용",
  "video_url": "https://cdn.groovo.io/video/42/index.m3u8",
  "duration_ms": 180000,
  "fps": 30,
  "thumbnail_url": "https://cdn.groovo.io/thumb/42.jpg"
}
```

> `keypoint_path`는 AI 서버만 사용하므로 클라이언트 응답에 포함하지 않습니다.
> 

**에러**

| 상태 | code | 상황 |
| --- | --- | --- |
| 404 | VIDEO_NOT_FOUND | 존재하지 않는 영상 |

---

## ④ 세션 (Session)

### POST /api/v1/sessions

분석 세션 생성. ws_token 발급 및 Redis에 세션 정보 기록.

**Request Body**

```json
{
  "video_id": 42
}
```

**처리 흐름**

1. JWT 검증 → user_id 확인
2. video_id 존재 여부 검증 (없으면 404)
3. session_id(UUID) 생성
4. ws_token(JWT, payload에 session_id 포함) 발급
5. Redis 기록: `session:{session_id}` → { user_id, video_id, status: "active" }
6. sessions 테이블에 레코드 생성 (status: active)

**Response 201**

```json
{
  "session_id": "550e8400-e29b-41d4-a716-446655440000",
  "ws_token": "eyJhbGciOi...",
  "ws_url": "wss://ai.groovo.io/ws/analyze",
  "expires_in": 1800
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| session_id | string | UUID, Redis 키 및 JWT와 동일 |
| ws_token | string | AI 서버 WebSocket 인증용 |
| ws_url | string | AI 서버 연결 주소 |
| expires_in | int | ws_token 만료까지 남은 초 (예: 1800) |

**에러**

| 상태 | code | 상황 |
| --- | --- | --- |
| 401 | UNAUTHORIZED | 토큰 없음/만료 |
| 404 | VIDEO_NOT_FOUND | 존재하지 않는 video_id |

> 세션 종료는 별도 클라이언트 호출이 아니라, AI 서버의 리포트 생성 통지(⑤ 참고)를 통해 처리됩니다.
> 

---

## ⑤ 리포트 (Report)

### POST /api/v1/internal/reports

AI 서버가 WebSocket 정상 종료 후 호출하는 **내부 엔드포인트**. 리포트를 생성하고 세션을 종료 처리합니다.

> 이 엔드포인트는 클라이언트가 아니라 AI 서버가 호출합니다. AI 서버와 백엔드가 같은 인스턴스에 있으므로 `http://localhost:8080/api/v1/internal/reports`로 호출합니다. nginx 설정에서 `/api/v1/internal/` 경로는 외부 접근을 차단합니다. 발전 시 서버 간 인증을 강화합니다.
> 

**Request Body**

```json
{
  "session_id": "550e8400-e29b-41d4-a716-446655440000",
  "average_score": 0.873,
  "detail_path": "reports/550e8400.json"
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| session_id | string | 대상 세션 |
| average_score | float | 전체 평균 일치율 |
| detail_path | string | S3에 업로드된 상세 시계열 경로 |

**처리 흐름**

1. 서버 간 인증 검증
2. session_id로 세션 조회 (없으면 404)
3. 멱등성 체크: 이미 리포트가 존재하면 중복 생성하지 않음
4. reports 테이블에 저장
5. sessions status를 finished로, finished_at 기록

**Response 201**

```json
{
  "report_id": 7001,
  "session_id": "550e8400-e29b-41d4-a716-446655440000"
}
```

> 데모는 직접 HTTP 호출. 발전 방향: Redis Queue 기반으로 변경 + 멱등성/재시도 강화.
> 

---

### GET /api/v1/reports

내 리포트 목록 조회 ("내 연습 기록")

**Request**: Authorization 헤더 필요. JWT의 user_id로 본인 리포트만 조회.

**Response 200**

```json
{
  "reports": [
    {
      "report_id": 7001,
      "video_title": "K-POP 기초 안무 1",
      "average_score": 0.873,
      "created_at": "2026-05-20T14:30:00Z"
    }
  ]
}
```

**에러**

| 상태 | code | 상황 |
| --- | --- | --- |
| 401 | UNAUTHORIZED | 토큰 없음/만료 |

---

### GET /api/v1/reports/{reportId}

리포트 상세 조회

**Request**: Authorization 헤더 필요. 본인 리포트만 접근 가능(user_id 검증).

**Response 200**

```json
{
  "report_id": 7001,
  "session_id": "550e8400-e29b-41d4-a716-446655440000",
  "video_id": 42,
  "video_title": "K-POP 기초 안무 1",
  "average_score": 0.873,
  "detail_url": "https://s3.../reports/550e8400.json?X-Amz-Signature=...",
  "detail_url_expires_in": 300,
  "created_at": "2026-05-20T14:30:00Z"
}
```

> 상세 시계열은 백엔드가 S3 presigned URL을 발급해 중계합니다. 클라이언트는 `detail_url`로 직접 S3에서 데이터를 받습니다. presigned URL은 만료시간(예: 300초)이 있어 조회 시점마다 새로 발급합니다.
> 

**에러**

| 상태 | code | 상황 |
| --- | --- | --- |
| 403 | FORBIDDEN | 본인 리포트가 아님 |
| 404 | REPORT_NOT_FOUND | 존재하지 않는 리포트 |

---
