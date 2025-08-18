# MyLog

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Java](https://img.shields.io/badge/Java-17-orange)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13+-336791)
![Redis](https://img.shields.io/badge/Redis-6+-DC382D)
![AWS S3](https://img.shields.io/badge/AWS-S3-FF9900)
![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED)

**블로그 형태의 소셜 미디어 플랫폼**

사용자들이 게시글을 작성하고, 댓글을 통해 소통하며, 카테고리와 태그로 콘텐츠를 관리하는 소셜 블로그 서비스

</div>

---

## 1. 개요

**MyLog**는 Spring Boot 기반의 블로그 스타일 소셜 미디어 플랫폼입니다. 사용자들이 게시글을 작성하고 공유하며, 댓글을 통해 소통할 수 있는 환경을 제공합니다. **OAuth2 소셜 로그인**, **AWS S3 이미지 스토리지**, **Redis 기반 토큰 관리**, **JWT 인증**을 통한 안전하고 확장 가능한 아키텍처를 제공합니다.


- **인증 및 권한 관리**: JWT 기반 인증 시스템과 OAuth2 소셜 로그인 (Google, Kakao, Naver)
- **게시글 관리**: 블로그 포스트 생성, 수정, 삭제 및 카테고리/태그 기반 검색
- **댓글 시스템**: 계층형 댓글 구조로 게시글에 대한 댓글 및 대댓글 지원
- **카테고리 관리**: 사용자별 커스텀 카테고리 생성 및 관리
- **알림 시스템**: 사용자 활동에 대한 실시간 알림 및 설정 관리
- **미디어 관리**: AWS S3 기반 이미지 업로드 및 관리
- **최적화**
  - [캐싱을 이용한 TPS 300% 개선](https://www.notion.so/29609de9fe418053b59add56a43d9b5a)
  - [N+1최적화를 통한 TPS 50% 개선](https://www.notion.so/N-1-29609de9fe418015b799f286a465d7e9)
  - [비동기를 적용을 통한 기능의 분리](https://www.notion.so/29609de9fe41806b8e52fa53a1126739?pvs=25)
  

---

## 2. 목차
- [주요 기능](#3-주요-기능)
- [기술 스택](#4-기술-스택)
- [아키텍처](#5-아키텍처)
- [API 문서](#6-api-문서)
- [프로젝트 구조](#7-프로젝트-구조)

---

## 3. 주요 기능

### 3.1 인증 및 권한 관리

#### JWT 기반 인증 시스템
- ✅ Access Token + Refresh Token 이중 토큰 관리
- ✅ Redis 기반 Refresh Token 캐싱 및 관리
- ✅ 자동 토큰 갱신 API를 통한 세션 유지
- ✅ Spring Security 기반 인증 필터 체인

#### OAuth2 소셜 로그인
- ✅ Google, Kakao, Naver 소셜 로그인 지원
- ✅ 첫 로그인 시 자동 회원 생성
- ✅ OAuth2 제공자별 프로필 정보 매핑

### 3.2 게시글 관리

#### 게시글 CRUD
- ✅ 게시글 생성, 조회, 수정, 삭제
- ✅ Markdown 형식 콘텐츠 지원
- ✅ 페이지네이션 지원 (기본 20개/페이지)
- ✅ 작성자 본인만 수정/삭제 가능한 권한 제어

#### 이미지 관리
- ✅ AWS S3 기반 이미지 업로드
- ✅ Multipart/form-data를 통한 이미지 및 데이터 동시 전송
- ✅ 이미지 URL 자동 생성 및 관리
- ✅ 게시글 삭제 시 S3 이미지 자동 정리

#### 카테고리 및 태그
- ✅ 사용자별 커스텀 카테고리 생성
- ✅ 다중 태그 지원 (Many-to-Many 관계)
- ✅ 카테고리/태그 기반 게시글 검색
- ✅ 키워드 검색 기능

### 3.3 댓글 시스템

#### 계층형 댓글 구조
- ✅ 댓글 및 대댓글 지원 (부모-자식 관계)
- ✅ 댓글 생성, 수정, 삭제
- ✅ 게시글별 댓글 목록 조회 (페이지네이션: 20개/페이지)
- ✅ 대댓글 목록 조회 (페이지네이션: 5개/페이지)

#### 댓글 관리
- ✅ 작성자 본인만 수정/삭제 가능
- ✅ 내가 작성한 댓글 목록 조회
- ✅ 내 게시글에 달린 댓글 목록 조회
- ✅ JPA Auditing을 통한 자동 타임스탬프 관리

### 3.4 카테고리 관리

- ✅ 사용자별 독립적인 카테고리 생성
- ✅ 카테고리 생성, 조회, 수정, 삭제
- ✅ 게시글과의 연관 관계 관리
- ✅ 카테고리 중복 검증

### 3.5 알림 시스템

- ✅ 사용자 활동 기반 알림 생성
- ✅ 알림 목록 조회 (페이지네이션)
- ✅ 읽음 처리 기능
- ✅ 알림 유형별 설정 관리 (활성화/비활성화)

---

## 4. 기술 스택

### 4.1 백엔드 프레임워크

| 기술 | 버전 | 용도 |
|------|------|------|
| **Spring Boot** | 3.x | 애플리케이션 프레임워크 |
| **Java** | 17 | 프로그래밍 언어 |
| **Spring Data JPA** | - | ORM 및 데이터 접근 계층 |
| **Spring Security** | - | 인증 및 권한 관리 |
| **Spring Validation** | - | 요청 데이터 유효성 검증 |
| **Lombok** | - | 보일러플레이트 코드 제거 |

### 4.2 데이터베이스 & 캐싱

| 기술 | 버전 | 용도 |
|------|------|------|
| **PostgreSQL** | 13+ | 관계형 데이터베이스 (프로덕션) |
| **Redis** | 6+ | 분산 캐싱 및 Refresh Token 저장 |

### 4.3 인증 & 보안

| 기술 | 버전 | 용도 |
|------|------|------|
| **OAuth2 Client** | - | 소셜 로그인 (Google, Kakao, Naver) |
| **JJWT** | - | JWT 토큰 생성 및 검증 |
| **Spring Security** | - | 인증 필터 체인 및 권한 관리 |

### 4.4 클라우드 & 스토리지

| 기술 | 버전 | 용도 |
|------|------|------|
| **AWS S3** | SDK 2.x | 게시글 및 프로필 이미지 저장소 |

### 4.5 모니터링 & 로깅

| 기술 | 버전 | 용도 |
|------|------|------|
| **Spring Actuator** | - | 헬스체크 및 메트릭 엔드포인트 |
| **Sentry** | - | 실시간 에러 트래킹 및 알림 |
| **SLF4J & Logback** | - | 애플리케이션 로깅 |

### 4.6 API 문서화

| 기술 | 버전 | 용도 |
|------|------|------|
| **SpringDoc OpenAPI 3** | - | Swagger UI 기반 REST API 문서 자동 생성 |

### 4.7 빌드 & 배포

| 기술 | 버전 | 용도 |
|------|------|------|
| **Gradle** | 8.x | 빌드 자동화 도구 |
| **Docker** | - | 컨테이너화 및 배포 |
| **Docker Compose** | - | 멀티 컨테이너 애플리케이션 관리 |
| **GitHub Actions** | - | CI/CD 파이프라인 |
| **JaCoCo** | - | 테스트 커버리지 분석 (80% 목표) |
| **JUnit 5** | - | 단위 테스트 프레임워크 |

---

## 5. 아키텍처

### 5.1 소프트웨어 아키텍쳐
![소프트웨어아키텍쳐](https://mylog-imgsource.s3.ap-northeast-2.amazonaws.com/mylog/software_architecture.png)


### 5.2 엔티티 관계도 (ERD)

![ERD](https://mylog-imgsource.s3.ap-northeast-2.amazonaws.com/mylog/ERD.png)

**엔티티 설계 패턴**:
- **JPA Auditing**: `@CreatedDate`, `@LastModifiedDate`로 타임스탬프 자동 관리
- **계층형 구조**: Comment 엔티티의 self-referencing으로 대댓글 구조 구현
- **다대다 관계**: ArticleTag 조인 테이블로 게시글-태그 관계 표현
- **OAuth 통합**: OAuthProvider enum으로 소셜 로그인 제공자 구분

### 5.3 인증 흐름도

#### 일반 로그인
```
사용자 → 이메일/비밀번호 제출 → Spring Security 검증
      → JWT Access Token 생성 → Refresh Token 생성
      → Refresh Token을 Redis 저장 → 클라이언트에 토큰 반환
```

#### 소셜 로그인
![소셜로그인](https://mylog-imgsource.s3.ap-northeast-2.amazonaws.com/mylog/authflow.png)


### 5.4 CI-CD
![CI-CD](https://mylog-imgsource.s3.ap-northeast-2.amazonaws.com/mylog/cicdflow.png)
---

## 6. API 문서

### 6.1 Swagger UI 접근


```
https://mylog-api.click/swagger-ui/index.html
```

### 6.2 API 응답 구조

모든 API 응답은 `ResponseService`를 통해 표준화된 구조를 따릅니다:

**성공 응답**:
```json
{
  "code": 1,
  "message": "성공하였습니다.",
  "data": { /* 실제 데이터 */ }
}
```

**에러 응답**:
```json
{
  "code": -1,
  "message": "에러 메시지"
}
```

**응답 타입**:
- `CommonResult`: 기본 성공/에러 응답 (데이터 없음)
- `SingleResult<T>`: 단일 객체 응답
- `ListResult<T>`: 리스트 응답
- `Page<T>`: 페이지네이션 응답 (Spring Data)

### 6.3 API 엔드포인트 상세

#### 6.3.1 인증 API (`/api/auth`)

| Method | Endpoint | Summary |
|--------|----------|---------|
| **POST** | `/api/auth/login` | 이메일 로그인 (LoginRequest → LoginResponse) |
| **POST** | `/api/auth/refresh` | Access Token 갱신 (RefreshRequest → RefreshResponse) |
| **POST** | `/api/auth/oauth/login` | 소셜 로그인 (OAuthRequest → LoginResponse) |

#### 6.3.2 회원 관리 API (`/api/members`)

| Method | Endpoint | Summary |
|--------|----------|---------|
| **POST** | `/api/members/sign-up` | 회원가입 (SignUpRequest) |
| **GET** | `/api/members/me` | 내 프로필 조회 (인증 필요) |
| **PUT** | `/api/members/me` | 프로필 수정 (multipart: UpdateMemberRequest + file) |
| **DELETE** | `/api/members/me` | 회원 탈퇴 (인증 필요) |

#### 6.3.3 게시글 API (`/api/articles`)

| Method | Endpoint | Summary |
|--------|----------|---------|
| **POST** | `/api/articles` | 게시글 생성 (multipart: ArticleCreateRequest + file) |
| **GET** | `/api/articles/{articleId}` | 게시글 단건 조회 |
| **PUT** | `/api/articles/{articleId}` | 게시글 수정 (multipart: ArticleUpdateRequest + file) |
| **DELETE** | `/api/articles/{articleId}` | 게시글 삭제 |
| **GET** | `/api/articles/all` | 전체 게시글 목록 (페이지네이션) |
| **GET** | `/api/articles/me` | 내 게시글 목록 (페이지네이션, 인증 필요) |
| **GET** | `/api/articles/all/search` | 전체 게시글 검색 (?keyword=&tag=) |
| **GET** | `/api/articles/me/search` | 내 게시글 검색 (?keyword=) |

#### 6.3.4 댓글 API (`/api`)

| Method | Endpoint | Summary |
|--------|----------|---------|
| **POST** | `/api/articles/{articleId}/comments` | 댓글 생성 |
| **GET** | `/api/articles/{articleId}/comments` | 게시글 댓글 목록 (페이지네이션) |
| **PUT** | `/api/comments/{commentId}` | 댓글 수정 |
| **DELETE** | `/api/comments/{commentId}` | 댓글 삭제 |
| **GET** | `/api/comments/me` | 내가 작성한 댓글 목록 (페이지네이션) |
| **GET** | `/api/articles/me/comments` | 내 게시글에 달린 댓글 목록 (페이지네이션) |

#### 6.3.5 카테고리 API (`/api/categories`)

| Method | Endpoint | Summary |
|--------|----------|---------|
| **POST** | `/api/categories` | 카테고리 생성 |
| **GET** | `/api/categories` | 내 카테고리 목록 조회 |
| **PUT** | `/api/categories/{categoryId}` | 카테고리 수정 |
| **DELETE** | `/api/categories/{categoryId}` | 카테고리 삭제 |

#### 6.3.6 알림 API (`/api/notifications`)

| Method | Endpoint | Summary |
|--------|----------|---------|
| **GET** | `/api/notifications` | 알림 목록 조회 (페이지네이션) |
| **PUT** | `/api/notifications/{id}` | 알림 읽음 처리 |

#### 6.3.7 알림 설정 API (`/api/notifications/settings`)

| Method | Endpoint | Summary |
|--------|----------|---------|
| **GET** | `/api/notifications/settings` | 알림 설정 목록 조회 |
| **PUT** | `/api/notifications/settings/{type}` | 알림 유형별 설정 토글 |

---

## 7. 프로젝트 구조

### 주요 디렉토리 설명

| 디렉토리 | 역할 | 주요 기능 |
|---------|------|----------|
| **annotations/** | 커스텀 어노테이션 | OAuth2 서비스 타입 지정 (`@OAuth2ServiceType`) |
| **classes/** | 공통 클래스 | 댓글 응답 모델 (`Reply`), 인증 사용자 (`CustomUser`) |
| **common/** | 응답 표준화 | 통일된 API 응답 구조 (`CommonResult`, `SingleResult`, `ListResult`) |
| **config/** | 인프라 설정 | JWT, S3, Redis, Security, QueryDSL, Cache, Async, Feign 설정 |
| **controller/** | REST API | 엔드포인트 정의 및 Swagger 문서화 (7개 컨트롤러) |
| **model/dto/** | 데이터 전송 객체 | 도메인별 Request/Response DTO (auth, article, comment, member 등) |
| **model/entity/** | 도메인 모델 | JPA 엔티티 및 연관 관계 정의 |
| **enums/** | 열거형 | OAuth 제공자 (`OAuthProvider`) |
| **exception/** | 커스텀 예외 | 도메인별 예외 클래스 및 전역 핸들러 |
| **repository/** | 데이터 접근 | 도메인별 Spring Data JPA 리포지토리 (QueryDSL 커스텀 구현 포함) |
| **service/** | 비즈니스 로직 | 도메인별 Read/Write 서비스 분리 패턴 |

### 상세 구조

```
mylog/
├── src/
│   ├── main/
│   │   ├── java/com/mylog/
│   │   │   ├── annotations/             # 커스텀 어노테이션
│   │   │   ├── classes/                 # 공통 클래스 (Reply, CustomUser)
│   │   │   ├── common/                  # API 응답 모델
│   │   │   ├── config/                  # 인프라 설정 (JWT, S3, Redis, Security 등)
│   │   │   ├── controller/              # REST 컨트롤러 (7개)
│   │   │   ├── enums/                   # 열거형 (OAuthProvider)
│   │   │   ├── exception/               # 커스텀 예외 및 전역 핸들러
│   │   │   ├── model/
│   │   │   │   ├── dto/                 # 데이터 전송 객체
│   │   │   │   │   ├── article/         (게시글 DTO)
│   │   │   │   │   ├── auth/            (인증 DTO)
│   │   │   │   │   ├── category/        (카테고리 DTO)
│   │   │   │   │   ├── comment/         (댓글 DTO)
│   │   │   │   │   ├── member/          (회원 DTO)
│   │   │   │   │   ├── notification/    (알림 DTO)
│   │   │   │   │   └── social/          (소셜 로그인 DTO - google, kakao, naver)
│   │   │   │   └── entity/              # JPA 엔티티 (8개)
│   │   │   │
│   │   │   ├── repository/              # 데이터 접근 계층
│   │   │   │   ├── article/             (게시글 리포지토리 + QueryDSL)
│   │   │   │   ├── category/            (카테고리 리포지토리)
│   │   │   │   ├── comment/             (댓글 리포지토리)
│   │   │   │   ├── member/              (회원 리포지토리)
│   │   │   │   ├── notification/        (알림 리포지토리)
│   │   │   │   └── ... (기타 리포지토리)
│   │   │   │
│   │   │   ├── service/                 # 비즈니스 로직
│   │   │   │   ├── article/             (게시글 서비스 - Reader/Service)
│   │   │   │   ├── category/            (카테고리 서비스)
│   │   │   │   ├── comment/             (댓글 서비스)
│   │   │   │   ├── member/              (회원 서비스)
│   │   │   │   ├── notification/        (알림 서비스)
│   │   │   │   ├── social/              (소셜 로그인 - google, kakao, naver)
│   │   │   │   └── ... (기타 서비스)
│   │   │   │
│   │   │   └── MylogApplication.java   # 메인 클래스
│   │   │
│   │   └── resources/
│   │       ├── application.yml          # 기본 설정
│   │       ├── application-dev.yml      # 개발 환경
│   │       └── application-prod.yml     # 프로덕션 환경
│   │
│   └── test/java/com/mylog/            # 단위 테스트 (20개 테스트 클래스)
│       └── service/                     # 도메인별 서비스 테스트
│
├── .github/workflows/
│   └── ci-cd.yaml                       # GitHub Actions CI/CD
├── docker-compose.yaml                  # Docker Compose 설정
├── Dockerfile                           # Docker 이미지 빌드
├── build.gradle                         # Gradle 빌드 설정
└── README.md                            # 프로젝트 문서
```
