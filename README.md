# Mylog

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-Cache-red.svg)](https://redis.io/)
[![Google Gemini](https://img.shields.io/badge/Google%20Gemini-AI-8E75B2.svg)](https://ai.google.dev/)

**Mylog**는 AI 기능을 탑재한 개인 블로그 플랫폼입니다.

사용자는 게시글을 작성하고 카테고리 및 태그로 분류할 수 있으며, Google Gemini API를 활용한 **AI 요약**과 **문체 변환** 기능을 통해 더 풍부한 콘텐츠를 경험할 수 있습니다. 소셜 로그인(Google, Kakao, Naver)을 지원하여 간편하게 가입하고, JWT 기반 인증으로 안전하게 서비스를 이용할 수 있습니다.

### 핵심 특징
- **AI 요약**: 게시글 작성 시 핵심 내용을 자동으로 요약
- **AI 문체 변환**: 친근한, 전문적인 등 다양한 문체로 게시글 변환
- **커스텀 문체**: 사용자만의 문체 스타일을 정의하여 적용
- **소셜 로그인**: Google, Kakao, Naver 계정으로 간편 가입

## 다이어그램
### 시스템 아키텍처
![시스템아키텍처](https://diagrams-noaahh.s3.ap-northeast-2.amazonaws.com/mylog-system-architecture.png)

### ERD
![ERD](https://diagrams-noaahh.s3.ap-northeast-2.amazonaws.com/mylog_erd.png)

## 주요 기능
<details>
<summary>자세히 보기</summary>

### 인증 (Auth)
- 일반 로그인 / 회원가입
- 소셜 로그인 (Google, Kakao, Naver)
- JWT 기반 인증 (Access Token + Refresh Token)
- 토큰 블랙리스트 관리

### 게시글 (Article)
- 게시글 CRUD
- 카테고리 분류
- 태그 시스템
- 게시글 검색
- **AI 요약 자동 생성** (비동기 처리)

### AI 기능
- **문체 변환**: 다양한 문체로 게시글 변환 (친근한, 전문적인, 유머러스한 등)
- **커스텀 문체**: 사용자 정의 문체 스타일 저장 및 적용
- **AI 요약**: 게시글 핵심 내용 자동 요약

### 댓글 (Comment)
- 댓글 작성/수정/삭제
- 대댓글 지원

### 알림 (Notification)
- 댓글 알림
- 알림 설정 관리

### 회원 (Member)
- 프로필 관리
- 알림 설정
- 커스텀 문체 스타일 관리

</details>

## API 명세서

<details>
<summary>자세히 보기</summary>

#### [api명세서링크](https://4nbr7ansok.apidog.io)  

### 인증 (Auth)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | 회원가입 | ❌ |
| POST | `/api/auth/login` | 이메일 로그인 | ❌ |
| POST | `/api/auth/logout` | 로그아웃 | ✅ |
| POST | `/api/auth/refresh` | 토큰 리프레시 | ❌ |
| POST | `/api/auth/oauth/login` | 소셜 로그인 (Google, Kakao, Naver) | ❌ |

### 게시글 (Article)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/articles` | 게시글 생성 (multipart/form-data) | ✅ |
| PUT | `/api/articles/{articleId}` | 게시글 수정 (multipart/form-data) | ✅ |
| DELETE | `/api/articles/{articleId}` | 게시글 삭제 | ✅ |
| GET | `/api/articles/{articleId}` | 게시글 상세 조회 | ❌ |
| GET | `/api/articles` | 게시글 목록/검색 조회 (`keyword`, `tag`, `categoryId` 쿼리) | ❌ |
| GET | `/api/articles/me` | 내 게시글 목록/검색 조회 | ✅ |
| POST | `/api/articles/transform-style` | AI 문체 변환 | ✅ |
| GET | `/api/articles/{articleId}/summary` | AI 요약 조회 | ❌ |

### 댓글 (Comment)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/articles/{articleId}/comments` | 댓글 생성 | ✅ |
| GET | `/api/articles/{articleId}/comments` | 댓글 목록 조회 (페이징) | ❌ |
| GET | `/api/comments/{parentId}/replies` | 대댓글 목록 조회 (페이징) | ❌ |
| GET | `/api/comments/me` | 내가 작성한 댓글 조회 | ✅ |
| GET | `/api/comments/me/received` | 내 게시글에 작성된 댓글 조회 | ✅ |
| PATCH | `/api/comments/{commentId}` | 댓글 수정 | ✅ |
| DELETE | `/api/comments/{commentId}` | 댓글 삭제 | ✅ |

### 카테고리 (Category)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/categories` | 카테고리 생성 | ✅ |
| GET | `/api/categories` | 카테고리 목록 조회 | ✅ |
| GET | `/api/categories/{categoryId}` | 카테고리 단일 조회 | ✅ |
| PUT | `/api/categories/{categoryId}` | 카테고리 수정 | ✅ |
| DELETE | `/api/categories/{categoryId}` | 카테고리 삭제 | ✅ |

### 회원 (Member)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/members/me` | 개인정보 조회 | ✅ |
| PATCH | `/api/members/me` | 개인정보 수정 (multipart/form-data) | ✅ |
| DELETE | `/api/members/me` | 회원 탈퇴 | ✅ |
| GET | `/api/members/me/notification-settings` | 알림 설정 조회 | ✅ |
| PUT | `/api/members/me/notification-settings/{type}` | 알림 토글 | ✅ |

### 커스텀 문체 스타일 (Custom Writing Style)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/members/me/custom-styles` | 커스텀 문체 스타일 생성 | ✅ |
| GET | `/api/members/me/custom-styles` | 커스텀 문체 스타일 목록 조회 | ✅ |
| PUT | `/api/members/me/custom-styles/{styleId}` | 커스텀 문체 스타일 수정 | ✅ |
| DELETE | `/api/members/me/custom-styles/{styleId}` | 커스텀 문체 스타일 삭제 | ✅ |

### 알림 (Notification)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/notifications` | 알림 목록 조회 (페이징) | ✅ |
| PUT | `/api/notifications/{id}` | 알림 읽음 처리 | ✅ |

### 외부 서비스 (External)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/external/gemini` | AI 텍스트 생성 테스트 | ❌ |

</details>

## 기술 스택

<details>
<summary>자세히 보기</summary>

### Backend
- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA** - ORM 및 데이터베이스 접근
- **Spring Security** - 인증/인가

### Database & Cache
- **PostgreSQL** - 메인 데이터베이스
- **Redis** - 토큰 저장 및 캐싱

### External Services
- **AWS S3** - 이미지 저장
- **Google Gemini API** - AI 요약 및 문체 변환

### Infrastructure
- **QueryDSL** - 동적 쿼리 작성
- **JWT (jjwt)** - 토큰 기반 인증
- **Springdoc OpenAPI** - API 문서화
- **Sentry** - 에러 추적
- **Prometheus + Micrometer** - 모니터링

</details>

## 프로젝트 구조

```
com.mylog/
├── common/                 # 공통 모듈
│   ├── annotations/        # 커스텀 어노테이션
│   ├── db/                 # BaseEntity
│   ├── enums/              # 열거형
│   ├── exception/          # 예외 처리
│   ├── resolver/           # ArgumentResolver
│   ├── response/           # 응답 객체
│   ├── security/           # 보안 관련
│   └── validation/         # 유효성 검사
├── config/                 # 설정 클래스
├── domain/                 # 도메인 계층
│   ├── article/            # 게시글 도메인
│   ├── auth/               # 인증 도메인
│   ├── category/           # 카테고리 도메인
│   ├── comment/            # 댓글 도메인
│   ├── member/             # 회원 도메인
│   └── notification/       # 알림 도메인
└── external/               # 외부 서비스 연동
    ├── gemini/             # Google Gemini API
    ├── oauth/              # OAuth 클라이언트
    ├── redis/              # Redis 서비스
    └── s3/                 # AWS S3 서비스
```
