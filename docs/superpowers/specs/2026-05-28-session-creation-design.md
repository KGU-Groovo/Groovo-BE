# 세션 생성 기능 설계 (Session Creation)

작성일: 2026-05-28

## 1. 개요와 범위

분석(댄스 연습) 세션을 생성하는 단일 엔드포인트 `POST /api/v1/sessions`를 구현한다.
요청을 받으면 영상 존재를 검증하고, `session_id`(UUID)를 발급하고, AI 서버 WebSocket
인증용 `ws_token`(JWT)을 발급하며, 세션 정보를 DB와 Redis에 기록한 뒤 응답한다.

여기서 말하는 "세션"은 **로그인 세션이 아니라 댄스 연습 세션**이다. 인증 세션과 무관하다.

### 범위에 포함 (In scope)
- `POST /api/v1/sessions` 엔드포인트 (생성만)
- 임시 사용자 식별(`X-User-Id` 헤더) seam — 향후 JWT 인증으로 교체할 자리
- `ws_token`(JWT) 발급 인프라 (`JwtProvider`) — 신규
- `sessions` 테이블/엔티티 (status=`active` 기록)
- Redis 세션 해시 기록 (`session:{id}`, TTL 30분)

### 범위에서 제외 (Out of scope, 향후 작업)
- 세션 종료(`finished`) 처리 — SPEC상 Report 내부 엔드포인트(`POST /api/v1/internal/reports`)에서 처리. 별도 작업.
- 실제 인증/인가 (Auth 도메인, `access_token`, 로그인/회원가입) — 별도 작업.
- `User` 엔티티 / `users` 테이블 — 별도 작업.
- `sessions.user_id` → `users` 외래키 제약 — `users`가 생긴 뒤 추가 (코드 주석으로 명시).

## 2. 의사결정 요약 (브레인스토밍 결과)

| 결정 | 선택 | 사유 |
| --- | --- | --- |
| Auth 의존성 | 임시 user_id로 세션만 먼저 | Auth/User 인프라가 아직 없음. 데모 우선, 추후 교체. |
| 사용자 식별 | `X-User-Id` 헤더 + ArgumentResolver | 컨트롤러 시그니처 불변으로 JWT 교체 가능. 멀티 유저 테스트 가능. |
| 세션 저장소 | DB + Redis | SPEC 흐름 그대로. Report가 나중에 sessions 조회/갱신. `video_id`는 실제 FK(`@ManyToOne`), `user_id`는 users 부재로 FK 없이(주석, 추후 추가). |
| 생명주기 범위 | 생성만 | finished 처리는 Report 작업에서. 세션 단위로 깔끔하게. |
| JWT 라이브러리 | jjwt | 표준, 깔끔한 API. `JwtProvider`는 재사용 가능하게 설계(추후 Auth가 access_token에 재사용). |
| Redis 구조 | Hash | SPEC의 구조화된 필드 맵과 자연스럽게 일치. |
| 인증 seam 위치 | `common/security` | Auth 도메인이 향후 재사용. common/은 nested 패키지 사용. |

## 3. API 계약

### POST /api/v1/sessions

분석 세션 생성. `ws_token` 발급 및 DB/Redis에 세션 정보 기록.
(컨텍스트 패스 `/api` + 컨트롤러 매핑 `/v1/sessions` = `/api/v1/sessions`, VideoController 패턴과 동일)

**Request Header**
- `X-User-Id: <userId>` (임시 인증 seam). 없거나 숫자가 아니면 401.

**Request Body** (snake_case)
```json
{
  "video_id": 42
}
```

**Response 201** (snake_case)
```json
{
  "session_id": "550e8400-e29b-41d4-a716-446655440000",
  "ws_token": "eyJhbGciOi...",
  "ws_url": "wss://ai.groovo.io/ws/analyze",
  "expires_in": 1800
}
```
> 응답은 `ApiResponseAdvice`에 의해 공통 `ApiResponse`로 자동 래핑된다.

**에러**

| 상태 | code | 상황 |
| --- | --- | --- |
| 400 | INVALID_INPUT_VALUE | `video_id` 누락/형식 오류 (body validation) |
| 401 | UNAUTHORIZED | `X-User-Id` 헤더 없음/형식 오류 |
| 404 | VIDEO_NOT_FOUND | 존재하지 않는 `video_id` |

생성용으로 **새 ErrorCode는 필요 없다** — 기존 `UNAUTHORIZED`, `VIDEO_NOT_FOUND`, `INVALID_INPUT_VALUE` 재사용.

## 4. 인증 seam (임시 → JWT 교체 지점)

`common/security` 패키지(`com.groovo.server.common.security`):

- `@CurrentUserId` — 파라미터 어노테이션.
- `CurrentUserIdArgumentResolver implements HandlerMethodArgumentResolver`
  - `supportsParameter`: 파라미터에 `@CurrentUserId`가 있고 타입이 `Long`.
  - `resolveArgument`: `X-User-Id` 헤더를 읽음.
    - 헤더가 없거나 공백 → `throw new BusinessException(ErrorCode.UNAUTHORIZED)`
    - `Long.parseLong` 실패 → `throw new BusinessException(ErrorCode.UNAUTHORIZED)`
    - 성공 → `Long` 반환.
  - 리졸버에서 던진 `BusinessException`은 `GlobalExceptionHandler(@RestControllerAdvice)`가 처리 → 401.
- 컨트롤러는 `create(@CurrentUserId Long userId, ...)`로 받는다.

**교체 전략**: 향후 실제 JWT가 들어오면 이 리졸버 내부를 `Authorization: Bearer` 파싱으로
바꾸거나, JWT 인증 필터 + `@AuthenticationPrincipal`로 전환한다. **컨트롤러 시그니처는 불변.**

리졸버 등록: `config/WebMvcConfig.java`(`implements WebMvcConfigurer`)에서
`addArgumentResolvers`로 등록.
> 이름은 `WebMvcConfig` — Spring Web MVC(서버의 HTTP 계층) 설정이라는 의미이며, 프론트가
> 웹/앱인지와 무관하다.

## 5. ws_token (JWT) 설계

신규 `JwtProvider`(`common/security`):

- 라이브러리: **jjwt** `0.12.x`. HS256 대칭키 서명.
- 재사용 가능한 범용 메서드로 설계 (추후 Auth가 `access_token`에 재사용):
  - 예: `String create(String subject, Map<String, Object> claims, Duration ttl)`
  - (파싱 메서드는 이번 범위에서 불필요 — 발급만. 추후 Auth 작업에서 추가.)
- ws_token claims:
  - `sub` = `session_id`
  - `userId`, `videoId`
  - `iat`, `exp` (만료 30분)
- 키: `Keys.hmacShaKeyFor(secret bytes)` — secret은 HS256 요건상 최소 256bit(32자) 이상.

### ⚠ 구현 시 최우선 검증 리스크: jjwt JSON serializer ↔ Jackson 버전
jjwt는 claims 직렬화를 위해 JSON serializer 모듈이 필요하다. 이 프로젝트는 **Jackson 3
(`tools.jackson.*`)** 를 사용한다. `jjwt-jackson`은 Jackson 2(`com.fasterxml.jackson`)
기반이라 클래스패스 충돌/중복 의존 소지가 있다.

- **권장**: `jjwt-gson` serializer 사용 — Gson을 내장해 Jackson 버전과 독립적. 가장 안전.
- 대안: `com.fasterxml.jackson`(Jackson 2)이 클래스패스에 존재하는지 확인 후 `jjwt-jackson` 사용.
- **구현 계획의 첫 단계에서 의존성 조합을 정하고 컴파일/서명 스모크 테스트로 검증한다.**

build.gradle 추가(권장안):
```gradle
implementation 'io.jsonwebtoken:jjwt-api:0.12.6'
runtimeOnly  'io.jsonwebtoken:jjwt-impl:0.12.6'
runtimeOnly  'io.jsonwebtoken:jjwt-gson:0.12.6'
```

## 6. 데이터 모델 (DB)

`session/domain/Session.java`, 테이블 `sessions`. (패키지는 디렉터리에 맞춘 **nested** 컨벤션 —
`com.groovo.server.session.domain` — 아래 §9 참고)

| 필드 | 타입 | 컬럼/비고 |
| --- | --- | --- |
| id | String (UUID) | `@Id` PK. JWT·Redis 키와 동일 값. `@GeneratedValue` 없이 서비스에서 UUID 주입. |
| userId | Long | `user_id`. **FK 제약 없음** (users 테이블 부재) — `// TODO: users 엔티티 생기면 @ManyToOne FK로 전환` 주석 명시. |
| video | `@ManyToOne(LAZY) Video` | `@JoinColumn(name="video_id", nullable=false)` → **실제 FK**. `videos`가 존재하므로 ERD대로 FK 매핑. videoId는 `video.getId()`로 접근. |
| status | enum `SessionStatus` | `@Enumerated(EnumType.STRING)`. 값: `ACTIVE`, `FINISHED`. 생성 시 `ACTIVE`. |
| startedAt | Instant | `started_at`. 생성 시각. |
| finishedAt | Instant (nullable) | `finished_at`. 생성 시 null. |

- `SessionStatus` enum: `session/domain/SessionStatus.java` (`{ ACTIVE, FINISHED }`).
- 엔티티 스타일은 `Video`를 따른다: `@NoArgsConstructor(access = PROTECTED)`, `@Getter`,
  `@Builder`(private 생성자). String PK는 `@GeneratedValue` 없이 빌더로 주입.
- `video`는 `@ManyToOne(fetch = LAZY, optional = false)`. `user_id`(Long)와 `video`(연관관계)의
  비대칭은 의도적 — video는 참조 대상이 존재하고 user는 아직 없기 때문. users 도입 시 대칭이 됨.
- 결합 주의: session 도메인이 `Video` 엔티티(`com.groovo.server.video.domain.Video`)를 import한다.
  ddl-auto 환경에서 FK를 얻는 표준 방식이며 ERD와 일치하므로 수용한다.
- DDL: dev 프로파일은 `JPA_DDL_AUTO=update`로 테이블 자동 생성. 별도 마이그레이션 파일 없음.
  (prod는 `validate` — 추후 정식 DDL/마이그레이션 필요. §11 참고.)

## 7. Redis 구조

`session/repository/SessionRedisStore.java` — `RedisTemplate<String, String>` 사용.

- key: `session:{session_id}`
- 자료구조: **Hash** (`opsForHash`). 필드(모두 String 직렬화):

| 필드 | 출처 |
| --- | --- |
| `user_id` | 요청 헤더(임시) |
| `video_id` | `video.getId()` (로드한 Video) |
| `keypoint_path` | Video 엔티티 `keypointPath` (AI 서버용) |
| `fps` | Video 엔티티 `fps` |
| `status` | `"active"` |
| `started_at` | epoch seconds |

- TTL: **30분** — `redisTemplate.expire(key, Duration.ofMinutes(30))`.
- `keypoint_path`, `fps`는 클라이언트 응답엔 없지만 Redis엔 저장한다(AI 서버가 사용).

## 8. 서비스 흐름 (`SessionService`)

`@Transactional`. 흐름:

1. `videoRepository.findById(videoId)` → 없으면 `BusinessException(VIDEO_NOT_FOUND)`(404).
   - 존재 시 `keypointPath`, `fps` 확보.
2. `sessionId = UUID.randomUUID().toString()`.
3. `JwtProvider`로 `ws_token` 발급 (claims: sub=sessionId, userId, videoId / 만료 30분).
4. `Session` 엔티티 저장 (`status=ACTIVE`, `startedAt=now`, `video`=1에서 로드한 Video,
   `userId`=헤더값) — `sessionRepository.save`. (이미 로드한 Video를 연관관계에 주입 → 추가 쿼리 없음)
5. `SessionRedisStore`로 해시 기록 + TTL 30분.
6. 응답 DTO 반환: `{ sessionId, wsToken, wsUrl, expiresIn=1800 }`.

- `ws_url`, 만료(30분), Redis TTL은 설정값(§10)에서 주입.
- DB 저장 후 Redis 기록 순서. Redis 작업은 JPA 트랜잭션 밖이지만, 예외 발생 시 메서드
  전파로 DB 트랜잭션은 롤백된다(데모 수준 정합성으로 충분).

## 9. 컴포넌트 / 파일 목록

> **패키지 컨벤션**: 각 파일은 물리적 디렉터리에 맞는 **nested** 패키지를 선언한다
> (예: `session/controller/` → `com.groovo.server.session.controller`,
> `session/domain/` → `...session.domain`, `session/repository/` → `...session.repository`,
> `session/dto/` → `...session.dto`). README의 도메인형 구조 및 정리된 `video` 도메인과 일치.
> `common/`, `config/`도 동일하게 nested.

### 신규
| 파일 | 패키지 | 역할 |
| --- | --- | --- |
| `common/security/CurrentUserId.java` | `...common.security` | 파라미터 어노테이션 |
| `common/security/CurrentUserIdArgumentResolver.java` | `...common.security` | `X-User-Id` → `Long` 해석, 실패 시 401 |
| `common/security/JwtProvider.java` | `...common.security` | JWT 발급(재사용 가능) |
| `common/security/JwtProperties.java` | `...common.security` | `@ConfigurationProperties("app.jwt")` record |
| `config/WebMvcConfig.java` | `...config` | `WebMvcConfigurer` — ArgumentResolver 등록 |
| `config/JwtConfig.java` | `...config` | `@EnableConfigurationProperties({JwtProperties, SessionProperties})` |
| `session/domain/Session.java` | `...session.domain` | 엔티티 |
| `session/domain/SessionStatus.java` | `...session.domain` | enum |
| `session/repository/SessionRedisStore.java` | `...session.repository` | Redis 해시 기록 + TTL |
| `session/dto/SessionCreateRequest.java` | `...session.dto` | `@NotNull Long videoId`, `@JsonNaming(SnakeCase)` |
| `session/dto/SessionCreateResponse.java` | `...session.dto` | `sessionId, wsToken, wsUrl, expiresIn`, `@JsonNaming(SnakeCase)` |

### 내용 채움 (현재 빈 파일)
| 파일 | 역할 |
| --- | --- |
| `session/controller/SessionController.java` | `POST /v1/sessions`, `@ResponseStatus(CREATED)`, `@CurrentUserId`, `@Valid @RequestBody` |
| `session/service/SessionService.java` | §8 흐름 |
| `session/repository/SessionRepository.java` | `JpaRepository<Session, String>` |

### 수정
| 파일 | 변경 |
| --- | --- |
| `build.gradle` | jjwt 의존성 추가 (§5) |
| `src/main/resources/application.yml` | `app.jwt`, `app.session` 설정 블록 추가 |
| `src/main/resources/application-dev.yml` / `-prod.yml` | 필요 시 프로파일별 secret/ws-url |
| `.env.example` | `JWT_SECRET`, `WS_TOKEN_EXPIRATION`, `SESSION_WS_URL` 추가 |
| 설정 등록 | 신규 `config/JwtConfig.java`에 `@EnableConfigurationProperties({JwtProperties.class, SessionProperties.class})` (S3Config 패턴) |

## 10. 설정 (application.yml)

S3Properties 패턴(record + `@ConfigurationProperties` + `Duration`)을 따른다.

```yaml
app:
  jwt:
    secret: ${JWT_SECRET:}            # HS256, 최소 32자. prod 필수.
    ws-token-expiration: ${WS_TOKEN_EXPIRATION:30m}
  session:
    ws-url: ${SESSION_WS_URL:wss://ai.groovo.io/ws/analyze}
```

- Redis TTL과 `expires_in`은 `ws-token-expiration`에서 파생(단일 출처).
- 프로퍼티 클래스는 **두 개의 record로 확정**(S3Properties 패턴):
  - `JwtProperties(String secret, Duration wsTokenExpiration)` — 추후 `accessTokenExpiration` 추가 여지.
  - `SessionProperties(String wsUrl)`.

## 11. 테스트 전략 (기존 Video 테스트 패턴 따름)

TDD로 구현. 기존 `VideoServiceTest`(단위, mock), `VideoControllerTest`(MockMvc) 패턴 참고.

### `SessionServiceTest` (단위, mock)
- video 없음 → `BusinessException(VIDEO_NOT_FOUND)`.
- 성공 → `sessionRepository.save` 호출(status=ACTIVE), `SessionRedisStore` 기록 호출,
  응답에 sessionId/wsToken/wsUrl/expiresIn 포함. (`JwtProvider`, repo, store는 mock)

### `SessionControllerTest` (MockMvc)
- `X-User-Id` 없음 → 401 `UNAUTHORIZED`.
- `X-User-Id` 비숫자 → 401.
- body `video_id` 누락 → 400 `INVALID_INPUT_VALUE`.
- 존재하지 않는 video → 404 `VIDEO_NOT_FOUND`.
- 정상 → 201 + 응답 body 필드 검증.

### `JwtProvider` 스모크
- 발급된 토큰이 서명 검증되고 claims(sub=sessionId, userId, videoId)와 만료가 포함되는지.
- (jjwt 의존성 조합 검증 겸용 — §5 리스크)

## 12. 향후 작업 / 마이그레이션 노트

- **실제 인증**: `X-User-Id` 리졸버 → JWT 파싱/필터로 교체. `JwtProvider`에 파싱 메서드 추가.
- **User 도메인 + FK**: `users` 생성 후 `sessions.user_id` FK 추가(엔티티 주석에 표시).
- **세션 종료**: Report 내부 엔드포인트에서 `status=FINISHED`, `finished_at` 기록 +
  Redis status 갱신/정리.
- **prod DDL**: `JPA_DDL_AUTO=validate`이므로 정식 스키마 마이그레이션(예: Flyway) 도입 검토.
