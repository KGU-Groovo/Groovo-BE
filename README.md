# Groovo Backend

Groovo 백엔드 서버입니다. Spring Boot 기반으로 REST API, MySQL, Redis, S3 presigned URL 인프라를 제공합니다.

이 문서는 백엔드 개발을 처음 시작하는 팀원이 로컬 환경을 띄우고, 프로젝트 구조를 이해한 뒤, 같은 규칙으로 기능을 추가할 수 있도록 작성했습니다.

## 1. Tech Stack

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Redis
- MySQL
- AWS SDK for Java v2 S3
- Gradle
- Docker Compose

## 2. Quick Start

프로젝트 루트에서 `.env.example`을 복사해 `.env`를 만듭니다.

```bash
cp .env.example .env
```

Docker Compose로 애플리케이션, MySQL, Redis를 실행합니다.

```bash
make up
```

상태를 확인합니다.

```bash
make ps
make health
```

로그를 확인합니다.

```bash
make app-logs
```

브라우저에서 Swagger UI를 엽니다.

```text
http://localhost:8080/swagger-ui.html
```

종료할 때는 아래 명령을 사용합니다.

```bash
make down
```

볼륨과 로컬 이미지를 포함해 완전히 정리하려면 아래 명령을 사용합니다.

```bash
make fclean
```

## 3. Environment

Docker Compose는 프로젝트 루트의 `.env`를 자동으로 읽어 컨테이너 환경변수로 전달합니다. Spring Boot는 전달받은 환경변수를 `application.yml`의 placeholder에서 사용합니다.

주요 환경변수:

| Key | Description | Default |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Spring profile | `dev` |
| `SERVER_PORT` | 애플리케이션 포트 | `8080` |
| `DB_HOST` | MySQL host | `localhost` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | MySQL database | `groovo` |
| `DB_USERNAME` | MySQL username | `groovo` |
| `DB_PASSWORD` | MySQL password | `change-me` |
| `MYSQL_ROOT_PASSWORD` | MySQL root password | `root` |
| `JPA_DDL_AUTO` | Hibernate DDL mode | `update` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `REDIS_PASSWORD` | Redis password | empty |
| `CACHE_TTL` | Redis cache TTL(ms) | `600000` |
| `AWS_REGION` | AWS region | `ap-northeast-2` |
| `AWS_S3_BUCKET` | S3 bucket name | empty |
| `AWS_S3_ENDPOINT` | S3-compatible endpoint | empty |
| `AWS_S3_PATH_STYLE_ACCESS_ENABLED` | Path-style S3 access | `false` |
| `AWS_S3_ACCESS_KEY` | S3 access key | empty |
| `AWS_S3_SECRET_KEY` | S3 secret key | empty |
| `AWS_S3_PRESIGNED_URL_EXPIRATION` | Presigned URL 만료 시간 | `5m` |

실제 비밀번호와 AWS 키는 `.env.example`에 넣지 않습니다. `.env`는 Git에 커밋하지 않습니다.

## 4. Useful URLs

애플리케이션 포트는 기본 `8080`입니다. `SERVER_PORT`를 바꾸면 아래 URL의 포트도 같이 바꿔서 사용합니다.

| Purpose | URL |
| --- | --- |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/api-docs` |
| Actuator health | `http://localhost:8080/actuator/health` |
| Simple health | `http://localhost:8080/health` |

## 5. Build And Test

테스트:

```bash
./gradlew test
```

빌드:

```bash
./gradlew build
```

로컬에서 애플리케이션만 직접 실행할 수도 있습니다. 이 경우 MySQL, Redis는 별도로 떠 있어야 하고 필요한 환경변수를 직접 제공해야 합니다.

```bash
./gradlew bootRun
```

## 6. Directory Structure

이 프로젝트는 도메인형 구조를 기본으로 합니다. 새 기능은 `controller`, `service`, `repository`, `domain`, `dto`를 도메인 패키지 아래에 모읍니다.

```text
src/main/java/com/groovo/server
├── ServerApplication.java
├── common
│   ├── exception       # 공통 예외 및 전역 예외 처리
│   ├── logging         # 요청/응답 로그, 메서드 실행 로그
│   └── response        # 공통 API 응답 래핑
├── config              # Spring 설정
├── health              # 헬스체크 API
├── infra
│   ├── redis           # Redis 큐 인프라
│   └── s3              # S3 presigned URL 인프라
└── {domain}
    ├── controller
    ├── domain
    ├── dto
    ├── repository
    └── service
```

예시:

```text
video
├── controller
├── domain
├── dto
├── repository
└── service
```

역할 기준:

- `controller`: HTTP 요청을 받고 응답을 반환합니다.
- `service`: 비즈니스 로직을 처리합니다.
- `repository`: DB 접근을 담당합니다.
- `domain`: Entity, 도메인 모델을 둡니다.
- `dto`: request/response DTO를 둡니다.
- `common`: 여러 도메인에서 공유하는 공통 기능을 둡니다.
- `infra`: Redis, S3처럼 외부 인프라 연동 코드를 둡니다.
- `config`: Spring Bean, Security, JPA 등 설정 코드를 둡니다.

## 7. API Rules

### Prefix

비즈니스 API는 `/api` prefix를 사용합니다.

```text
GET    /api/videos
POST   /api/videos
GET    /api/videos/{videoId}
PATCH  /api/videos/{videoId}
DELETE /api/videos/{videoId}
```

운영/문서/상태 확인용 endpoint는 `/api` prefix를 붙이지 않습니다.

```text
/health
/actuator/health
/api-docs
/swagger-ui.html
```

### Naming

- URL path는 소문자와 kebab-case를 사용합니다.
- 리소스 이름은 복수형을 기본으로 합니다.
- path variable은 도메인명을 포함해 명확하게 작성합니다.

```text
좋음: /api/videos/{videoId}
지양: /api/video/{id}
```

### HTTP Method

- `GET`: 조회
- `POST`: 생성 또는 명령성 작업
- `PATCH`: 일부 수정
- `PUT`: 전체 교체
- `DELETE`: 삭제

생성 API에서 `201 Created`가 필요하면 `@ResponseStatus(HttpStatus.CREATED)` 또는 `ResponseEntity.status(HttpStatus.CREATED)`를 사용합니다.

## 8. Common Implementations

### Response Wrapper

`ApiResponseAdvice`는 컨트롤러가 반환한 JSON 응답을 `ApiResponse`로 자동 래핑합니다.

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {},
  "timestamp": "..."
}
```

이미 `ApiResponse`인 응답과 `ProblemDetail`은 중복 래핑하지 않습니다. HTTP status는 컨트롤러의 `@ResponseStatus` 또는 `ResponseEntity` 설정을 따릅니다.

### Exception Handling

`GlobalExceptionHandler`가 공통 예외 응답을 처리합니다.

- `BusinessException`: 서비스에서 의도한 비즈니스 예외
- `MethodArgumentNotValidException`: request body validation 실패
- `ConstraintViolationException`: parameter validation 실패
- `AuthenticationException`: 인증 실패
- `AccessDeniedException`: 인가 실패
- `Exception`: 처리되지 않은 예외

비즈니스 예외는 `ErrorCode`를 기준으로 던집니다.

```java
throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
```

### Logging

요청/응답 로그는 `RequestLoggingFilter`가 처리합니다.

```text
[REQUEST] requestId=... method=POST path=/api/... clientIp=... body=...
[RESPONSE] requestId=... method=POST path=/api/... status=200 elapsedMs=... body=...
```

컨트롤러 메서드 실행 시간과 파라미터 요약은 `LoggingAspect`가 처리합니다.

```text
[METHOD] requestId=... handler=VideoController.create elapsedMs=... args=... result=...
[EXCEPTION] requestId=... handler=VideoController.create elapsedMs=... args=... exception=...
```

요청/응답 body는 JSON, text, XML 계열만 로그로 남기고, multipart/binary 계열은 본문을 남기지 않습니다.

### S3

사용자 파일 업로드는 서버가 파일을 직접 받지 않고 presigned URL을 발급하는 방식을 사용합니다.

기본 흐름:

```text
1. 클라이언트가 서버에 업로드 URL 요청
2. 서버가 S3 key를 정하고 presigned PUT URL 발급
3. 클라이언트가 presigned URL로 S3에 직접 업로드
4. 클라이언트가 서버에 업로드 완료 또는 key 저장 요청
```

`S3ObjectStorage`에서 사용하는 주요 함수:

```java
createUploadPresignedUrl(String key, String contentType)
createDownloadPresignedUrl(String key)
delete(String key)
objectUrl(String key)
```

`contentType`을 포함해 presigned URL을 만들면 클라이언트가 업로드할 때 같은 `Content-Type` 헤더를 보내야 합니다.

### Redis

`RedisTemplate<String, String>`은 문자열 직렬화로 설정되어 있습니다. `RedisQueuePublisher`, `RedisQueueConsumer`는 Redis list를 이용한 간단한 queue publish/consume 기능을 제공합니다.

## 9. Git Workflow

### Branch Rules

브랜치는 `main`, `dev`, `feat/*`를 기준으로 관리합니다.

- `main`: 운영 배포 기준 브랜치입니다.
- `dev`: 개발 통합 브랜치입니다. 기능 브랜치는 기본적으로 `dev`에서 분기합니다.
- `feat/*`: 기능 개발 브랜치입니다.

권장 흐름:

```bash
git switch dev
git pull
git switch -c feat/video-upload
```

작업이 끝나면 `feat/*`에서 `dev`로 PR을 생성합니다. `main` 병합은 배포 시점에 별도로 진행합니다.

### Commit Rules

커밋 타입은 아래 기준을 사용합니다.

```text
feat: 새로운 기능
fix: 버그 수정
chore: 빌드, 설정, 의존성, 기타 작업
refact: 동작 변경 없는 리팩터링
docs: 문서 수정
```

예시:

```bash
git commit -m "feat: S3 presigned URL 발급 기능 추가"
git commit -m "fix: 요청 body 로깅 시 빈 본문 처리"
git commit -m "docs: 로컬 실행 방법 추가"
```

## 10. Development Notes

- 새 도메인은 `com.groovo.server.{domain}` 아래에 도메인형 구조로 추가합니다.
- 공통 응답 포맷이 적용되므로 컨트롤러에서는 일반 DTO를 반환하는 것을 기본으로 합니다.
- 비밀값은 `.env`, CI/CD Secret, 서버 환경변수로 관리합니다.
- 운영 profile에서는 `JPA_DDL_AUTO=validate`를 기본으로 사용합니다.
- 기능 작업 전 `dev`를 최신화하고, 작업 후 `./gradlew test`를 실행합니다.
