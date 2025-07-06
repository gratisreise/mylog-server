✦ MyLog 프로젝트 백엔드 분석 보고서

1. 개요

프로젝트 목적
MyLog는 사용자들이 블로그 형태의 게시글을 작성하고 관리하며, 다른 사용자들과 소통할 수 있는 소셜 미디어 플랫폼입니다.

주요 기능
- 사용자 관리: 소셜 로그인(Google, Kakao, Naver) 및 이메일을 통한 회원가입 및 인증
- 게시글/댓글: 게시글 및 댓글 CRUD, 검색, 태그 기반 필터링
- 카테고리/태그: 콘텐츠 분류를 위한 카테고리 및 태그 관리
- 알림: 사용자 활동에 대한 실시간 알림
- 파일 업로드: AWS S3를 이용한 이미지 업로드

기술 스택
- 언어: Java 17
- 프레임워크: Spring Boot 3.2.5, Spring Security, Spring Data JPA
- 데이터베이스: PostgreSQL (H2는 개발용으로 사용)
- 캐시: Redis (리프레시 토큰 관리)
- 인증: JWT, OAuth 2.0
- 빌드: Gradle
- CI/CD: GitHub Actions, Docker, Docker Compose

2. 아키텍처 분석

프로젝트 구조
전형적인 계층형 아키텍처(Layered Architecture)를 따르고 있습니다.
- Controller: API 엔드포인트 정의 및 요청/응답 처리
- Service: 비즈니스 로직 처리
- Repository: 데이터 영속성 관리 (JPA)
- Config: 보안, JWT, CORS, S3, Redis 등 설정 관리
- DTO: 계층 간 데이터 전송 객체
- Entity: JPA 엔티티 모델

common 패키지를 통해 공통 응답 포맷을 표준화하여 API 응답의 일관성을 유지하고 있습니다.

주요 모듈 및 역할
- 인증 모듈: Spring Security와 JWT, OAuth2를 결합하여 강력한 인증/인가 시스템을 구축했습니다. JwtAuthenticationFilter를 통해
  모든 요청에 대한 JWT 유효성을 검증합니다.
- 소셜 로그인: OAuth2UserService 인터페이스와 OAuth2UserServiceFactory를 통해 Google, Kakao, Naver 등 다양한 OAuth 제공자를
  유연하게 확장할 수 있는 구조를 갖추고 있습니다.
- 파일 관리: S3Service를 통해 AWS S3와의 연동을 캡슐화하여 이미지 업로드/삭제 로직을 중앙에서 관리합니다.
- 알림 모듈: NotificationService를 통해 사용자에게 비동기적으로 알림을 보낼 수 있는 기반을 마련했습니다.

의존성 관리
build.gradle을 통해 의존성을 관리하며, Spring Boot Starter를 활용하여 버전 충돌을 최소화하고 있습니다.
spring-cloud-starter-openfeign을 통해 외부 API와의 통신을 간결하게 처리하고 있습니다.

3. 코드 품질 분석

코드 스타일 및 컨벤션
- 전반적으로 일관된 코드 스타일을 유지하고 있습니다.
- Lombok을 사용하여 Boilerplate 코드를 줄이고 코드 가독성을 높였습니다.
- @RequiredArgsConstructor를 통한 생성자 주입을 사용하여 의존성 주입(DI)을 명확하게 표현하고 있습니다.

객체지향 설계 원칙
- SRP (단일 책임 원칙): ArticleReadService와 ArticleWriteService처럼 읽기/쓰기 책임을 분리하여 CQRS(Command and Query
  Responsibility Segregation) 패턴을 일부 적용하려는 시도가 보입니다.
- OCP (개방-폐쇄 원칙): OAuth2UserServiceFactory는 새로운 소셜 로그인 제공자를 추가할 때 기존 코드를 수정하지 않고 확장할 수
  있도록 설계되어 OCP를 잘 준수하고 있습니다.
- DIP (의존관계 역전 원칙): 대부분의 경우 인터페이스에 의존하기보다는 구체 클래스에 직접 의존하고 있어, 향후 유연성 및 테스트
  용이성 측면에서 개선의 여지가 있습니다.

예외 처리 전략
GlobalExceptionHandler를 통해 애플리케이션 전역에서 발생하는 예외를 중앙에서 처리하고 있습니다. CInvalidDataException,
CUnAuthorizedException 등 비즈니스 상황에 맞는 커스텀 예외를 정의하여 예외 상황을 명확하게 식별할 수 있도록 한 점은 좋은
설계입니다.

4. API 설계 분석

RESTful API 설계
- ArticleController, MemberController 등 리소스 중심으로 API를 설계하여 RESTful 원칙을 잘 따르고 있습니다.
- HTTP 메서드(GET, POST, PUT, DELETE)를 적절하게 사용하여 각 행위의 의도를 명확히 표현합니다.

인증 및 인가
- Authorization 헤더의 Bearer 토큰을 통해 JWT 기반 인증을 수행합니다.
- Spring Security의 antMatchers를 사용하여 엔드포인트별 접근 권한을 효과적으로 제어하고 있습니다.

DTO 활용
- Request와 Response에 각각 DTO를 사용하여 API의 명세를 명확히 하고, 엔티티 객체가 외부에 직접 노출되는 것을 방지하여 보안을
  강화했습니다.
- ArticleCreateRequest, ArticleResponse 등 각 DTO는 목적에 맞게 잘 분리되어 있습니다.

5. 데이터베이스 설계 분석

ERD (추정)
제공된 엔티티 코드를 기반으로 추정한 ERD는 다음과 같습니다.
- Member와 Article, Comment는 1:N 관계
- Article과 Tag는 ArticleTag를 통해 N:M 관계
- Category는 계층 구조를 가질 수 있으며 Article과 1:N 관계

JPA 엔티티 설계
- @EntityListeners(AuditingEntityListener.class)를 통해 생성/수정 시간을 자동으로 기록하고 있습니다.
- @Builder를 사용하여 객체 생성의 유연성을 확보했습니다.
- ArticleTag와 같이 복합 키를 사용하는 경우 @EmbeddedId를 통해 잘 처리하고 있습니다.

Repository 관리
Spring Data JPA를 사용하여 보일러플레이트 코드 없이 데이터베이스 작업을 수행하고 있습니다. MemberRepository의 findByEmail과
같이 메서드 이름을 통해 쿼리를 생성하는 기능을 잘 활용하고 있습니다.

6. 보안 분석

인증 및 인가
- 비밀번호는 BCryptPasswordEncoder를 사용하여 안전하게 해싱하여 저장합니다.
- JWT의 시크릿 키와 유효 기간은 application.yml에서 관리되어 유연한 설정이 가능합니다.

시크릿 관리
- compose.yaml과 ci-cd.yaml에서 볼 수 있듯이, 데이터베이스 비밀번호, JWT 시크릿 키, AWS 키 등 민감한 정보는 GitHub Secrets를
  통해 안전하게 관리되고 있습니다. 이는 매우 훌륭한 보안 실천 사례입니다.

기타 보안 고려사항
- CORS(Cross-Origin Resource Sharing) 설정을 통해 localhost:3000에서의 요청만 허용하여 웹 애플리케이션의 보안을 강화했습니다.
- CSRF(Cross-Site Request Forgery) 보호는 비활성화되어 있으나, JWT를 사용하는 Stateless 서버에서는 일반적인 설정입니다.

7. CI/CD 파이프라인 분석

GitHub Actions 워크플로우
- 테스트 자동화: push 및 pull_request 이벤트 발생 시 자동으로 단위 테스트를 실행하여 코드의 안정성을 보장합니다.
- 빌드 및 배포 자동화: main 브랜치에 push가 발생하면 애플리케이션을 빌드하고 Docker 이미지를 생성하여 Docker Hub에 푸시한 후,
  EC2에 자동으로 배포합니다.
- 동적 IP 등록: GitHub Actions 러너의 IP를 동적으로 AWS 보안 그룹에 등록하여 안전한 배포 환경을 구축했습니다.

Docker 설정
- Multi-stage 빌드: Dockerfile에서 Multi-stage 빌드를 사용하여 최종 이미지의 크기를 최소화하고 빌드 환경과 실행 환경을
  분리하여 보안을 강화했습니다.
- Docker Compose: compose.yaml을 통해 mylog-app과 redis 서비스를 한 번에 관리하여 개발 및 배포의 편의성을 높였습니다.

8. 개선 제안

아키텍처
- Service 계층 인터페이스 도입: 현재 Service 계층이 구체 클래스로만 구성되어 있습니다. 인터페이스를 도입하면 향후 다른
  구현체로 쉽게 교체할 수 있으며, 테스트 용이성이 향상됩니다.
- 알림 모듈 고도화: 현재 NotificationService는 동기적으로 동작하는 것으로 보입니다. Kafka나 RabbitMQ 같은 메시지 큐를
  도입하여 알림 발송을 비동기적으로 처리하면 시스템의 응답성을 높이고 다른 서비스와의 결합도를 낮출 수 있습니다.

코드 품질
- DTO 변환 로직 분리: DTO와 Entity 간의 변환 로직을 각 DTO 클래스 내의 정적 메서드나 별도의 Mapper 클래스(e.g., MapStruct)로
  분리하면 Service 계층의 코드가 더 간결해지고 책임이 명확해집니다.

API 설계
- 페이지네이션(Pagination) 적용: ArticleController의 게시글 목록 조회 API에 페이지네이션을 적용하여 대량의 데이터를
  효율적으로 처리하고 클라이언트의 부담을 줄일 수 있습니다.

보안
- Rate Limiting 적용: API 게이트웨이나 Spring Cloud Gateway를 도입하여 특정 IP나 사용자의 과도한 요청을 제한(Rate
  Limiting)하면 DDoS 공격으로부터 시스템을 보호할 수 있습니다.

테스트
- 통합 테스트 추가: 현재 단위 테스트 위주로 구성되어 있습니다. 실제 데이터베이스와 연동하는 통합 테스트를 추가하여 각 계층
  간의 상호작용이 올바르게 동작하는지 검증하면 시스템의 신뢰성을 더욱 높일 수 있습니다.