![image.png](attachment:d1ebf075-bdc5-4c5e-b033-ab737e4452c6:image.png)

- ERD 설명
    
    ### ERD란?
    
    ERD(Entity Relationship Diagram)는 서비스에서 저장해야 하는 데이터들을 표로 정리하고, 표들 사이의 관계를 선으로 나타낸 설계도야. 각 표(테이블)는 데이터의 종류를 나타내고, 표 안의 행(필드)은 저장할 정보의 항목을 나타낸다.
    
    ---
    
    ### 테이블 설명
    
    **users — 사용자**
    
    Groovo에 가입한 사용자 정보를 저장해. 자체 비밀번호 대신 소셜 로그인(OAuth)을 쓰기 때문에, `provider`(어떤 소셜인지)와 `provider_id`(그 소셜에서의 고유 ID) 조합으로 사용자를 식별한다. 같은 소셜 계정으로 중복 가입하지 못하도록 `(provider, provider_id)`는 중복 불가이고, `email`도 중복 불가라 한 이메일은 한 계정에만 쓸 수 있어. 비밀번호 원문이나 해시는 저장하지 않는다 — 인증은 소셜 제공자에게 맡기는 구조임
    
    | 필드 | 설명 |
    | --- | --- |
    | id | 사용자를 구분하는 고유 번호 (PK) |
    | email | 사용자 이메일, 중복 불가 |
    | nickname | 앱에서 표시되는 이름 |
    | profile_image_url | 프로필 이미지 주소 (없을 수 있음) |
    | provider | 소셜 로그인 제공자 (KAKAO / GOOGLE / APPLE) |
    | provider_id | 소셜 제공자가 발급한 사용자 고유 ID |
    | role | 권한 등급 (USER: 일반 사용자 / ADMIN: 관리자) |
    | status | 계정 상태 (ACTIVE: 활성 / WITHDRAWN: 탈퇴) |
    | created_at / updated_at | 가입일 / 마지막 정보 수정일 |
    
    ---
    
    **videos — 레퍼런스 댄스 영상**
    
    사용자가 따라 출 기준이 되는 댄스 영상의 정보를 저장한다.
    실제 영상 파일과 keypoint 파일은 S3(파일 저장 서버)에 있고, DB에는 그 위치(경로)만 저장한다.
    
    | 필드 | 설명 |
    | --- | --- |
    | id | 영상을 구분하는 고유 번호 (PK) |
    | title / description | 영상 제목과 설명 |
    | video_url | S3에 저장된 영상 재생 주소 |
    | keypoint_path | S3에 저장된 관절 좌표 파일(.npy) 위치 |
    | fps | 영상의 초당 프레임 수 (동기화에 사용) |
    | duration_ms | 영상 전체 길이 (밀리초 단위) |
    | thumbnail_url | 영상 목록에서 보여줄 썸네일 이미지 주소 |
    
    > `keypoint_path`와 `fps`는 실시간 분석을 위해 존재하는 필드이다. AI 서버는 이 두 값을 이용해서 사용자가 현재 몇 초 지점에 있는지 계산하고, 그에 맞는 기준 관절 데이터를 가져와 비교한다.
    > 
    
    ---
    
    **sessions — 분석 세션**
    
    사용자가 특정 영상으로 댄스 연습을 시작할 때마다 하나의 세션이 생성된다. 
    즉, "누가 어떤 영상을 언제 연습했는지"를 기록하는 테이블
    
    | 필드 | 설명 |
    | --- | --- |
    | id | 세션 고유 ID (UUID 문자열, JWT·Redis 키와 동일한 값 사용) |
    | user_id | 어떤 사용자의 세션인지 (users 참조) |
    | video_id | 어떤 영상으로 연습한 세션인지 (videos 참조) |
    | status | 현재 세션 상태 (active: 진행 중 / finished: 종료) |
    | started_at | 연습 시작 시각 |
    | finished_at | 연습 종료 시각 (종료 전엔 비어있음) |
    
    > `id`가 일반 숫자(bigint)가 아니라 문자열(varchar)인 이유는, 이 값이 JWT 토큰과 Redis 캐시에서 공통으로 사용되는 식별자이기 때문. 세 곳에서 같은 값을 쓰도록 맞춘 의도적인 설계
    > 
    
    ---
    
    **reports — 분석 리포트**
    
    세션이 종료되면 해당 세션의 결과 리포트가 생성된다. 세션 1개당 리포트 1개가 만들어지는 1:1 관계임.
    
    | 필드 | 설명 |
    | --- | --- |
    | id | 리포트 고유 번호 (PK) |
    | session_id | 어떤 세션의 리포트인지 (sessions 참조) |
    | user_id | 어떤 사용자의 리포트인지 |
    | video_id | 어떤 영상에 대한 리포트인지 |
    | average_score | 전체 연습의 평균 일치율 (0.0 ~ 1.0) |
    | detail_path | 프레임별 상세 점수 데이터 파일의 S3 경로 |
    
    > `user_id`와 `video_id`가 sessions에도 있는데 reports에도 있는 건 중복처럼 보일 수 있지만 이는, "내 리포트 목록 보기" 같은 기능을 구현할 때 sessions 테이블을 거치지 않고 reports 테이블만 조회해도 되도록 미리 저장해두는 것이다. (**역정규화)**
    > 
    
    ---
    
    ### 테이블 간 관계
    
    ERD의 선은 테이블 사이의 관계를 나타내며, Groovo ERD의 관계를 자연어로 표현하면 아래와 같다.
    
    | 관계 | 의미 |
    | --- | --- |
    | users → sessions | 한 사용자는 여러 세션을 가질 수 있어 (1:N) |
    | videos → sessions | 한 영상은 여러 세션에서 사용될 수 있어 (1:N) |
    | sessions → reports | 하나의 세션은 정확히 하나의 리포트를 가져 (1:1) |
    | users → reports | 한 사용자는 여러 리포트를 가질 수 있어 (1:N) |
    | videos → reports | 한 영상은 여러 리포트에서 참조될 수 있어 (1:N) |
    
    데이터 흐름 순서로 보면 이렇게 이어진다:
    
    ```
    사용자 가입 → users 레코드 생성
         ↓
    영상 선택 후 연습 시작 → sessions 레코드 생성 (status: active)
         ↓
    연습 종료 → sessions 업데이트 (status: finished)
              → reports 레코드 생성
    ```
