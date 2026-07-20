# 📌 Miraero Backend

> Miraero(미래로) 백엔드 서버입니다.
> 

목표 기반 자산 관리 서비스를 위한 REST API를 제공합니다.

---

## 📖 프로젝트 소개

Miraero는
목표(로드맵)를 설정하여 효율적으로 자산을 관리할 수 있도록 돕는 서비스입니다.

백엔드는 사용자 인증, 자산 조회, 목표 관리,
AI 서비스 연동을 위한 REST API를 제공합니다.

---

## 👨‍💻 백엔드 팀

| <img src="https://github.com/leeyoungheon.png" width="120"> | <img src="https://github.com/itleo29.png" width="120"> | <img src="https://github.com/tmj5574.png" width="120"> |
| --- | --- | --- |
| **이영헌** | **김영진** | **탁민주** |
| [GitHub](https://github.com/leeyoungheon) | [GitHub](https://github.com/itleo29) | [GitHub](https://github.com/tmj5574) |

## 🛠 Tech Stack

| 언어 | 프레임워크 | 데이터베이스 & ORM | 보안 & 인증 | API & 외부 서비스 | 빌드 & 배포 |
| :-: | :-: | :-: | :-: | :-: | :-: |
|<img src="https://www.vectorlogo.zone/logos/java/java-icon.svg" width="40" height="40" alt="Java" /> | <img src="https://www.vectorlogo.zone/logos/springio/springio-icon.svg" width="40" height="40" alt="Spring" /> | <img src="https://www.vectorlogo.zone/logos/mysql/mysql-icon.svg" width="40" height="40" alt="MySQL" /> <img src="https://mybatis.org/images/mybatis-logo.png" width="70" alt="MyBatis" /> | <img width="40" height="40" alt="jwt" src="https://github.com/user-attachments/assets/d36f48e6-d2a6-4b58-9a97-3cb0d3b45b69" />| <img width="40" height="40" alt="gpt" src="https://github.com/user-attachments/assets/01c80489-da8d-4267-9981-f9d7a27bfc84" />  | <img src="https://www.vectorlogo.zone/logos/gradle/gradle-icon.svg" width="40" height="40" alt="Gradle" />|
| Java | Spring | MySQL, MyBatis | JWT | ChatGPT API | Gradle |

---

## ✨ 주요 기능

| Feature | Description |
| --- | --- |
| 🎯 **맞춤형 금융 목표 설정** | 목표 금액과 기간을 설정하여 돈 모으기 또는 대출 상환 목표를 생성합니다. |
| 💳 **자산 연동** | 계좌와 저금통을 목표에 연결하여 자산을 통합 관리합니다. |
| 📈 **목표 진행 현황** | 목표 달성률, 현재 자산, 적정 페이스를 비교하여 진행 상황을 제공합니다. |
| 🔄 **자동이체 관리** | 목표 달성을 위한 자동이체를 등록하고 관리합니다. |
| 🤖 **AI 금융 로드맵** | 금융 성향과 자산 정보를 분석하여 맞춤형 금융 로드맵을 제공합니다. |
| 💡 **금융 상품 추천** | 목표와 금융 성향에 맞는 금융 상품을 추천합니다. |

---

## 🗄 데이터베이스

ERD

> ERD 이미지 추가 예정
> 

---

## 🏛 아키텍

Architecture Diagram

> 아키텍처 이미지 추가 예정
> 

---

## 📂 프로젝트 구조

```
backend
├── src
│   ├── main
│   │   ├── java
│   │   │   └── org.jejuro.miraero
│   │   │       └── ...
│   │   └── webapp
│   └── test
└── build.gradle
```

---

## 📚 API

| Domain | Description |
| --- | --- |
| Auth | 인증 및 인가 |
| User | 사용자 관리|
| Goal | 목표 관리 |
| Account | 계좌 관리|
| Asset | 자산 관리|
| Transaction | 거래 |
| AI | AI 서비스 |

Swagger 또는 API 명세 링크 추가 예정

---

## ⚙️ 환경변수

```
DB_URL=
DB_USERNAME=
DB_PASSWORD=

JWT_SECRET=
JWT_ACCESS_TOKEN_EXPIRATION=
JWT_REFRESH_TOKEN_EXPIRATION=

AI_API_KEY=
```

---

## 🚀 Getting Started

## Clone Repository

```bash
git clone https://github.com/team-miraero/backend.git
cd backend
```

---

## Prerequisites

다음 환경이 필요합니다.

- Java 17
- Gradle 8.x
- MySQL 8.0+
- Apache Tomcat 10.x

---

## Configure Database

MySQL에 데이터베이스를 생성합니다.

```sql
CREATE DATABASE miraero DEFAULT CHARACTER SET utf8mb4;
```

---

## Build Project

```bash
./gradlew clean build
```

Windows

```bash
gradlew.bat clean build
```

---

## Run Server

Tomcat에 프로젝트를 배포한 후 실행합니다.

또는 IntelliJ에서 Tomcat을 이용하여 실행합니다.

---

## Access

브라우저에서 접속합니다.

```
http://localhost:8080/
```

---

# 🌿 Git Convention

## 1. Branch Convention

모든 작업 브랜치는 **`main` 브랜치에서 생성**합니다.

| Branch | Description |
| --- | --- |
| `main` | 운영(배포) 브랜치 |
| `feature/#이슈번호-기능명` | 새로운 기능 개발 |
| `fix/#이슈번호-기능명` | 버그 수정 |
| `refactor/#이슈번호-기능명` | 코드 리팩토링 |
| `docs/#이슈번호-기능명` | 문서 수정 |
| `test/#이슈번호-기능명` | 테스트 코드 작성 및 수정 |
| `chore/#이슈번호-작업명` | 설정, 의존성, 빌드 환경 변경 |

### Example

```
main
 ├── feature/#21-goal-create
 ├── feature/#22-goal-read
 ├── fix/#31-jwt
 └── refactor/#42-goal
```

---

## 2. Commit Convention

### Commit Message Format

```
type: 작업 내용
```

### Commit Types

| Type | Description |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 코드 리팩토링 (기능 변경 없음) |
| `docs` | 문서 수정 |
| `style` | 코드 스타일 변경 (포맷팅 등) |
| `test` | 테스트 코드 작성 및 수정 |
| `chore` | 빌드, 설정 파일, 의존성 변경 |

#### Example

```
feat: 목표 생성 API 구현
feat: 목표 조회 API 구현
fix: JWT 토큰 검증 오류 수정
refactor: GoalService 비즈니스 로직 개선
docs: README 업데이트
style: 코드 포맷팅 적용
chore: Gradle 의존성 업데이트
```

---

## 3. Git Workflow

### 1. Issue 생성

- 작업 시작 전 GitHub Issue를 생성합니다.
- 하나의 작업은 하나의 Issue로 관리합니다.

### 2. Branch 생성

```bash
git switch main
git pull origin main
git switch -c feature/#21-goal-create
```

### 3. 개발 및 Commit

```bash
git add .
git commit -m "feat: 목표 생성 API 구현"
```

### 4. Push

```bash
git push origin feature/#21-goal-create
```

### 5. Pull Request 생성

- PR 제목은 Commit Convention을 따릅니다.
- PR 본문에 관련 Issue를 연결합니다.

```
Closes #21
```

### 6. Code Review

- 최소 1명의 승인(Approve) 후 Merge합니다.
- Merge 완료 시 연결된 Issue는 자동으로 종료됩니다.

### ✅ Rules

- 하나의 Issue는 하나의 기능만 담당합니다.
- 하나의 브랜치는 하나의 Issue만 작업합니다.
- 하나의 PR은 하나의 Issue만 포함합니다.
- 모든 브랜치는 `main`에서 생성합니다.
- Merge는 Code Review 완료 후 진행합니다.

---

# 📏 Code Convention

| Category | Convention |
| --- | --- |
| Package | 도메인별 패키지 구성 |
| Class | PascalCase |
| Method | camelCase |
| Variable | camelCase |
| Constant | UPPER_SNAKE_CASE |
| DTO | Request / Response 분리 |
| Exception | Global Exception Handler 사용 |
| API | RESTful API 설계 |
| SQL | MyBatis Mapper 사용 |

## 🔥 Troubleshooting

> 프로젝트 진행 중 발생한 주요 이슈와 해결 과정을 기록할 예정입니다.

---

## 📜 License

This project was developed as part of the KB IT's Your Life 7th Final Project.
