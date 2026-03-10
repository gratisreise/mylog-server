# Mylog

**Mylog**는 AI 기능을 탑재한 개인 블로그 플랫폼입니다.

사용자는 게시글을 작성하고 카테고리 및 태그로 분류할 수 있으며, Google Gemini API를 활용한 **AI 요약**과 **문체 변환** 기능을 통해 더 풍부한 콘텐츠를 경험할 수 있습니다. 소셜 로그인(Google, Kakao, Naver)을 지원하여 간편하게 가입하고, JWT 기반 인증으로 안전하게 서비스를 이용할 수 있습니다.

### 핵심 특징
- **AI 요약**: 게시글 작성 시 핵심 내용을 자동으로 요약
- **문체 변환**: 친근한, 전문적인 등 다양한 문체로 게시글 변환
- **커스텀 문체**: 사용자만의 문체 스타일을 정의하여 적용
- **소셜 로그인**: Google, Kakao, Naver 계정으로 간편 가입
- **실시간 알림**: 댓글 작성 시 알림 제공

## 아키텍처

### CQRS Service Pattern
각 도메인은 조회와 명령을 분리한 Reader/Writer 서비스 패턴을 사용합니다.

```java
// 조회 전용
@Service
@Transactional(readOnly = true)
public class ArticleReader {
    public Article getById(Long id) { ... }
    public Page<Article> getArticles(Pageable pageable) { ... }
}

// 명령 전용
@Service
@Transactional
public class ArticleWriter {
    public Article create(Article article) { ... }
    public void delete(Long id) { ... }
}
```

### OAuth2 Factory Pattern
`@OAuth2ServiceType` 어노테이션과 Factory를 통해 OAuth 제공자별 서비스를 분리합니다.

```java
@OAuth2ServiceType(OauthProvider.GOOGLE)
public class GoogleOAuthUserService extends AbstractOAuth2UserService { ... }
```

### 통일된 응답 구조
모든 API는 일관된 응답 형식을 사용합니다.

```json
{
  "code": "1",
  "message": "성공하였습니다.",
  "data": { }
}
```

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

## 기술 스택

<details>
<summary>자세히 보기</summary>

### Backend
- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA** - ORM 및 데이터베이스 접근
- **Spring Security** - 인증/인가
- **Spring OAuth2 Client** - 소셜 로그인

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
