# Testing, Configuration and Operations Rules

이 문서는 설정, Docker, Git, 테스트, 문서화와 작업 완료 검증에 적용합니다.
작업 전 최상위 `AGENTS.md`도 함께 확인합니다.

## 1. 설정

- `application.properties`는 DB, Redis, JWT, CORS, 외부 API, 업로드,
  MyBatis, 인코딩, 로깅처럼 목적별 구역으로 관리합니다.
- Spring Boot 전용 설정 키를 그대로 사용하지 않습니다.
- MyBatis와 인코딩 설정은 Spring Legacy Java Config에서 실제로 읽고 적용되게 작성합니다.
- 환경별 URL, Origin, 경로, 로그 레벨은 코드에서 분리합니다.
- 비밀값은 환경변수나 Git에서 제외된 비공개 설정으로 주입합니다.
- 공개 예시 파일에는 필요한 키와 빈 값 또는 안전한 예시만 둡니다.

## 2. Docker

- Docker Compose로 MySQL 8.0과 Redis 로컬 환경을 통일합니다.
- MySQL 데이터는 Docker Volume으로 보존합니다.
- Redis 보존 정책은 사용 목적에 맞게 정합니다.
- 컨테이너명, 포트, DB명은 팀 공통 설정을 사용합니다.
- 비밀번호와 API Key를 `docker-compose.yml`에 직접 작성하지 않습니다.
- 실제 환경변수 파일은 커밋하지 않고 키만 표시한 예시 파일을 제공합니다.
- Docker 변경 시 다른 팀원의 개발환경 영향을 확인합니다.

공개 예시 환경변수에는 필요에 따라 다음 키를 둡니다.

```properties
DB_HOST=
DB_PORT=
DB_NAME=
DB_USERNAME=
DB_PASSWORD=
REDIS_HOST=
REDIS_PORT=
JWT_SECRET=
FSS_API_KEY=
OPENAI_API_KEY=
OPENAI_DEFAULT_MODEL=
```

## 3. Git 관리

- IDE 개인 설정, Gradle·Java 빌드 결과, Node 의존성·빌드 결과,
  로그, 런타임 파일, 사용자 업로드 파일을 Git에서 제외합니다.
- `.env`, 로컬·비밀 설정과 민감정보 파일을 제외합니다.
- `.env.example`, 공개 설정 예시와 Gradle Wrapper는 포함합니다.
- Windows·macOS 자동 생성 파일을 제외합니다.
- `.gitignore` 변경 시 팀 공유 대상 소스나 설정까지 제외되지 않는지 확인합니다.

## 4. 테스트

커밋 또는 PR 전 애플리케이션 빌드와 주요 API 동작을 확인합니다.
버그 수정에는 가능하면 재발 방지 테스트를 추가합니다.

새 기능은 관련 있는 다음 경우를 검토합니다.

- 정상 요청
- 필수값 누락, 빈 값, 잘못된 형식
- 존재하지 않는 데이터
- 다른 사용자의 데이터 접근
- 빈 목록과 `null`
- 중복 데이터와 상태 충돌
- 외부 API 실패
- 금액 0
- 잘못되거나 만료된 목표 기간

## 5. API·DB 문서와 변경 공유

- API 추가·수정 시 Swagger 또는 API 명세를 함께 갱신합니다.
- 요청·응답, 상태 코드, 오류 코드를 명확히 작성합니다.
- API URL, 요청·응답 변경은 PR 본문에 기록합니다.
- DB 구조, API 응답 형식, 환경 변수 변경은 작업 전에 팀 공유 대상으로 알립니다.
- DB 변경 SQL과 영향 범위를 PR에 기록합니다.
- 다른 팀원의 기능에 영향을 주는 변경은 임의로 진행하지 않습니다.

## 6. 완료 기준

다음을 모두 확인한 뒤 완료로 보고합니다.

- 요구 기능과 주요 예외 상황이 정상 동작합니다.
- 기존 구조, 네이밍과 공통 응답을 유지합니다.
- 인증 기능은 인증과 소유권을 검증합니다.
- 애플리케이션 빌드 오류가 없습니다.
- 주요 API와 관련 테스트를 확인했습니다.
- API 문서와 DB 변경 SQL이 반영되었습니다.
- DB 컬럼, Mapper 인터페이스, XML SQL이 일치합니다.
- 임시 코드, 디버깅 로그, 사용하지 않는 import를 제거했습니다.
- 코드, SQL, 로그에 민감정보가 없습니다.
- 변경 파일, 핵심 변경사항과 검증 방법을 설명합니다.
- 실행하지 못한 검증이 있다면 이유를 명확히 밝힙니다.

