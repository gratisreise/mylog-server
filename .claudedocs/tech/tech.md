# MyLog 프로젝트 기술서

## 1. 프로젝트 개요

**MyLog**는 Spring Boot 기반의 블로그형 소셜 미디어 플랫폼입니다. 게시글, 댓글, 카테고리, 태그 기능을 제공하며, OAuth2 인증(Google, Kakao, Naver), AWS S3 이미지 저장소, Redis 리프레시 토큰 관리, JWT 기반 인증을 지원합니다.

### 1.1 기술 스택

| 구분 | 기술 |
|------|------|
| **Framework** | Spring Boot 3.2.5 |
| **Language** | Java 17 |
| **Build Tool** | Gradle 8.2.1 (Multi-module) |
| **Database** | PostgreSQL 42.7.3 (운영), H2 2.2.224 (테스트) |
| **Cloud** | Spring Cloud 2023.0.0 |
| **Query** | QueryDSL 5.1.0 |
| **Cache** | Caffeine Cache 3.2.2 |
| **Storage** | AWS S3 SDK 2.25.16 |
| **Session** | Redis 8.0.2-alpine |
| **Auth** | JWT (jjwt) 0.12.5 |
| **Docs** | Springdoc OpenAPI 2.5.0 |
| **Monitoring** | Sentry 8.12.0 |

---

## 2. 아키텍처

### 2.1 멀티 모듈 구조

```
mylog/
├── api/          # Application Layer (Controllers, Application Services)
├── domain/       # Domain Layer (Entities, Domain Services, Repositories)
├── common/       # Shared Utilities (Response, Exceptions, Enums)
├── infra/        # Infrastructure (Redis, S3, External APIs)
```

### 2.2 모듈 의존성

```
api --> infra --> common
api --> domain --> common
api --> common
```

### 2.3 CQRS 패턴 (Read/Write 분리)

도메인 서비스 레이어에서 읽기/쓰기 작업을 명확히 분리:

| Reader Service | Writer Service |
|----------------|----------------|
| ArticleReader | ArticleWriter |
| MemberReader | MemberWriter |
| CategoryReader | CategoryWriter |
| CommentReader | CommentWriter |
| NotificationReader | NotificationWriter |
| TagReader | TagWriter |

```java
// ArticleReader.java - 읽기 전용
@Service
@Transactional(readOnly = true)
public class ArticleReader {
    public Article getById(Long articleId) { ... }
    public Page<ArticleProjection> getArticles(Pageable pageable) { ... }
}

// ArticleWriter.java - 쓰기 전용
@Service
@Transactional
public class ArticleWriter {
    public Article createArticle(Article article) { ... }
    public void deleteArticle(Long articleId) { ... }
}
```

---

## 3. 도메인 엔티티

### 3.1 BaseEntity (JPA Auditing)

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### 3.2 Member 엔티티

```java
@Entity
@Table(indexes = {
    @Index(name = "idx_provider_providerId", columnList = "provider,providerId", unique = true)
})
public class Member extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30) private String email;
    @Column(length = 150) private String password;
    @Column(length = 30, nullable = false) private String memberName;
    @Column(length = 100, unique = true) private String nickname;
    @Column(length = 300, nullable = false) private String profileImg;
    @Column(length = 200) private String bio;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OauthProvider provider;  // LOCAL, SOCIAL, GOOGLE, KAKAO, NAVER

    @Column(length = 200) private String providerId;
}
```

### 3.3 Article 엔티티

```java
@Entity
public class Article extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 30, nullable = false) private String title;
    @Column(length = 3000, nullable = false) private String content;
    @Column(length = 300) private String articleImg;
}
```

### 3.4 ArticleTag (다대다 연결 테이블)

```java
@Entity
@IdClass(ArticleTagId.class)
public class ArticleTag {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Article article;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
```

### 3.5 ERD 요약

```
┌─────────────┐       ┌─────────────┐       ┌─────────────┐
│   Member    │       │   Article   │       │  Category   │
├─────────────┤       ├─────────────┤       ├─────────────┤
│ id          │──┐    │ id          │──┐    │ id          │
│ email       │  │    │ member_id   │◄─┘    │ member_id   │
│ password    │  │    │ category_id │◄──────►│ name        │
│ memberName  │  │    │ title       │       └─────────────┘
│ nickname    │  │    │ content     │
│ profileImg  │  │    │ articleImg  │       ┌─────────────┐
│ provider    │  │    └─────────────┘       │     Tag     │
│ providerId  │  │            │             ├─────────────┤
└─────────────┘  │            │             │ id          │
                 │            ▼             │ tagName     │
                 │    ┌─────────────┐       └─────────────┘
                 │    │ ArticleTag  │             │
                 │    ├─────────────┤             │
                 │    │ article_id  │◄────────────┘
                 │    │ tag_id      │
                 │    └─────────────┘
                 │
                 │    ┌─────────────┐
                 │    │   Comment   │
                 │    ├─────────────┤
                 └───►│ id          │
                      │ member_id   │
                      │ article_id  │
                      │ parentId    │
                      │ content     │
                      └─────────────┘
```

---

## 4. 보안 구현

### 4.1 Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final String[] WHITELISTED_URLS = {
        "/api/auth/**",
        "/api/members/sign-up",
        "/swagger-ui/**",
        "/v3/api-docs/**",
        "/h2-console/**",
        "/actuator/**",
        "/api/tests/**",
        "/api/articles/all/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService) {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new ExceptionHandlerFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JwtAuthenticationFilter(...), UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 4.2 JWT 구현

```java
@Component
public class JwtUtil {
    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessValidity;
    private final long refreshValidity;

    public JwtUtil(@Value("${jwt.access_secret}") String accessKey, ...) {
        this.accessKey = Keys.hmacShaKeyFor(accessKey.getBytes());
        this.refreshKey = Keys.hmacShaKeyFor(refreshKey.getBytes());
    }

    // HMAC SHA-512 알고리즘 사용
    public String createAccessToken(String subject, long memberId) {
        return Jwts.builder()
            .subject(subject)
            .claim("memberId", memberId)
            .issuedAt(now)
            .expiration(validity)
            .signWith(accessKey, Jwts.SIG.HS512)
            .compact();
    }

    public boolean validateAccessToken(String token) {
        Jwts.parser().verifyWith(accessKey).build().parseSignedClaims(token);
        return true;
    }
}
```

### 4.3 JWT 인증 필터

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {
        String jwt = resolveToken(request);
        if (StringUtils.hasText(jwt) && tokenProvider.validateAccessToken(jwt)) {
            String username = tokenProvider.getUsername(jwt);
            if(!tokenBlackListService.isLogout(username, jwt)){
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

### 4.4 OAuth2 팩토리 패턴

```java
@Component
public class OAuth2UserServiceFactory {
    private final Map<OauthProvider, OAuth2UserService> serviceMap = new HashMap<>();

    public OAuth2UserServiceFactory(List<OAuth2UserService> services) {
        for (OAuth2UserService service : services) {
            OAuth2ServiceType type = service.getClass().getAnnotation(OAuth2ServiceType.class);
            if (type != null) {
                serviceMap.put(type.value(), service);
            }
        }
    }

    public OAuth2UserService getOAuth2UserService(OauthProvider provider){
        return serviceMap.get(provider);
    }
}
```

**커스텀 어노테이션:**
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface OAuth2ServiceType {
    OauthProvider value();
}
```

**Google 구현 예시:**
```java
@OAuth2ServiceType(OauthProvider.GOOGLE)
public class GoogleOAuth2UserService extends AbstractOAuth2UserService {
    // OpenFeign 클라이언트로 토큰 및 사용자 정보 조회
}
```

### 4.5 토큰 블랙리스트 (Redis)

```java
@Service
public class TokenBlackListService {
    private static final String KEY_PREFIX = "BL:";

    public void accessTokenBlack(String authHeader){
        String accessToken = extractAccessToken(authHeader);
        long expiration = jwtUtil.getExpiration(accessToken);
        redisTemplate.opsForValue().set(
            KEY_PREFIX + accessToken,
            accessToken,
            Duration.ofMillis(expiration)
        );
    }

    public boolean isLogout(String username, String accessToken){
        String storedToken = redisTemplate.opsForValue().get(generateKey(username));
        return storedToken != null && storedToken.equals(accessToken);
    }
}
```

---

## 5. API 설계

### 5.1 통합 응답 구조

**성공 응답:**
```json
{
  "code": "1",
  "message": "성공하였습니다.",
  "data": { /* 실제 데이터 */ }
}
```

**에러 응답:**
```json
{
  "code": "COM001",
  "message": "중복된 데이터가 존재합니다.",
  "data": null
}
```

### 5.2 ResponseService

```java
public class ResponseService {
    public static <T> SingleResult<T> getSingleResult(T data) { ... }
    public static <T> ListResult<T> getListResult(List<T> data) { ... }
    public static CommonResult getSuccessResult() { ... }
    public static CommonResult getFailResult(BusinessException ex) { ... }
}
```

### 5.3 PageResponse

```java
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {
    public static <T> PageResponse<T> from(Page<T> page) { ... }
}
```

### 5.4 REST 컨트롤러 목록

| Controller | Base Path | 주요 엔드포인트 |
|------------|-----------|----------------|
| AuthController | `/api/auth` | `/login`, `/logout`, `/refresh`, `/oauth/login` |
| ArticleController | `/api/articles` | CRUD, `/me`, `/all`, `/me/search`, `/all/search` |
| CommentController | `/api` | `/articles/{id}/comments`, `/comments/me` |
| CategoryController | `/api/categories` | CRUD |
| MemberController | `/api/members` | `/me` (GET, PUT, DELETE) |
| NotificationController | `/api/notifications` | GET, `/{id}`, `/settings` |

---

## 6. 데이터베이스 최적화

### 6.1 QueryDSL 설정

```java
@Configuration
public class QuerydslConfig {
    @PersistenceContext
    private EntityManager em;

    @Bean
    public JPAQueryFactory jpaQueryFactory(){
        return new JPAQueryFactory(em);
    }
}
```

### 6.2 커스텀 레포지토리 - STRING_AGG 태그 집계

```java
private final String TAG_FUNCTION = "STRING_AGG({0}, ',' ORDER_BY {0}) ASC";

public Page<ArticleProjection> findAllCustom(Pageable pageable) {
    return queryFactory.select(Projections.constructor(
            ArticleProjection.class,
            article.id,
            article.title,
            article.member.memberName,
            article.category.categoryName,
            article.content,
            article.articleImg,
            Expressions.stringTemplate(TAG_FUNCTION, tag.tagName),
            article.createdAt,
            article.updatedAt)
        ).from(article)
        .leftJoin(articleTag).on(article.id.eq(articleTag.article.id))
        .leftJoin(tag).on(articleTag.tag.id.eq(tag.id))
        .groupBy(article.id)
        .orderBy(article.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();
}
```

### 6.3 쿼리 메서드

| 메서드 | 설명 |
|--------|------|
| `findAllCustom(Pageable)` | 전체 게시글 + 태그 |
| `findMineByMember(Long memberId, Pageable)` | 사용자 게시글 |
| `searchAll(String keyword, String tagName, Pageable)` | 전체 검색 |
| `searchMine(String keyword, String tagName, Pageable, Long memberId)` | 내 게시글 검색 |

---

## 7. 캐싱 및 성능 최적화

### 7.1 Caffeine Cache

```java
@Service
@Transactional(readOnly = true)
public class ArticleReader {
    @Cacheable(value = "articles", key="#pageable.getPageNumber()")
    public Page<ArticleProjection> getArticles(Pageable pageable){
        return articleRepository.findAllCustom(pageable);
    }

    @Cacheable(value = "articles", key="'태그='+#tag")
    public Page<ArticleProjection> getArticles(String keyword, String tag, Pageable pageable){
        return articleRepository.searchAll(keyword, tag, pageable);
    }
}
```

### 7.2 비동기 설정

```java
@Configuration
public class AsyncConfig implements AsyncConfigurer, AsyncUncaughtExceptionHandler {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("Async");
        executor.initialize();
        return executor;
    }
}
```

**S3 비동기 삭제:**
```java
@Async
public void deleteImage(String url) {
    String fileKey = url.substring(url.lastIndexOf("/") + 1);
    s3Client.deleteObject(DeleteObjectRequest.builder()
        .bucket(bucketName)
        .key(fileKey)
        .build());
}
```

---

## 8. 예외 처리

### 8.1 BusinessException 계층

```java
@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

### 8.2 ErrorCode 인터페이스

```java
public interface ErrorCode {
    String getMessage();
    String getCode();
}
```

### 8.3 CommonError Enum

```java
@Getter
@AllArgsConstructor
public enum CommonError implements ErrorCode {
    DUPLICATED_EMAIL("COM001", "중복된 데이터가 존재합니다."),
    FAILED_IMAGE_UPLOAD("COM002", "이미지 업로드에 실패했습니다."),
    REFRESH_TOKEN_UNDELETED("COM003", "리프레쉬 토큰이 삭제되지 않았습니다."),
    INVALID_TOKEN("COM004", "유효하지 않은 토큰입니다.");
}
```

### 8.4 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult handleBusinessException(BusinessException ex){
        return ResponseService.getFailResult(ex);
    }

    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult handleRestClientException(RestClientException ex){ ... }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult handleRuntimeException(RuntimeException ex){ ... }
}
```

---

## 9. 인프라 구성

### 9.1 Docker Compose (Blue-Green 배포)

```yaml
services:
  mylog-blue:
    container_name: mylog-blue
    ports: ["8080:8080"]
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    depends_on:
      redis: { condition: service_healthy }

  mylog-green:
    container_name: mylog-green
    ports: ["8081:8080"]

  redis:
    image: redis:8.0.2-alpine
    command: redis-server --port 6379 --requirepass ${REDIS_PASSWORD}
    healthcheck:
      test: ["CMD-SHELL", "redis-cli", "-a", "${REDIS_PASSWORD}", "ping"]
```

### 9.2 Redis 설정

```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }
}
```

**Redis 활용:**
- 리프레시 토큰: `RT:{username}` -> refresh token
- 토큰 블랙리스트: `BL:{accessToken}` -> accessToken

### 9.3 AWS S3 설정

```java
@Configuration
public class S3Config {
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .region(Region.AP_NORTHEAST_2)
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)))
            .build();
    }
}
```

**S3Service 메서드:**
- `upload(MultipartFile file)`: UUID 프리픽스로 파일 업로드, S3 URL 반환
- `deleteImage(String url)`: 비동기 이미지 삭제

### 9.4 OpenFeign 설정

```java
@Configuration
@EnableFeignClients(basePackages = "com.mylog")
public class FeignConfig {
    @Bean
    public Encoder feignFormEncoder() {
        return new FormEncoder();
    }
}
```

---

## 10. 테스트 아키텍처

### 10.1 테스트 구조

```java
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @InjectMocks
    private ArticleService articleService;

    @Mock private ArticleReader articleReader;
    @Mock private ArticleWriter articleWriter;
    @Mock private ArticleTagWriter articleTagWriter;
    @Mock private MemberReader memberReader;
    @Mock private CategoryReader categoryReader;
    @Mock private TagReader tagReader;
    @Mock private S3Service s3Service;
    @Mock private TagWriter tagWriter;

    private static final Long MEMBER_ID = 1L;
    private static final Long ARTICLE_ID = 100L;

    @BeforeEach
    void setUp() {
        member = Member.builder()
            .id(MEMBER_ID)
            .email("tester@example.com")
            .nickname("테스터")
            .build();
        customUser = new CustomUser(member,
            Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("게시글 작성 성공")
    void 게시글_작성_성공() {
        // Given - Mock behavior setup
        // When - Execute method
        // Then - Verify interactions
        verify(articleWriter, times(1)).createArticle(any(Article.class));
        verify(tagWriter, times(1)).getTagsOrCreate(tagNames);
    }
}
```

### 10.2 테스트 패턴

- MockitoExtension으로 의존성 주입
- BDD 스타일 모킹 (`given().willReturn()`)
- 정적 테스트 상수로 가독성 확보
- 한글 `@DisplayName`으로 비즈니스 컨텍스트 명확화

### 10.3 커버리지 요구사항

- **최소 커버리지**: 서비스 클래스 80% 라인 커버리지 (JaCoCo 강제)
- **커버리지 제외**: DTO, Entity, Config, Enum, Abstract 클래스

---

## 11. 주요 설계 결정 및 패턴

| 패턴 | 설명 |
|------|------|
| **멀티 모듈 아키텍처** | API, Domain, Common, Infrastructure 명확한 분리 |
| **CQRS 서비스 분리** | Reader/Writer 서비스로 트랜잭션 경계 명확화 |
| **팩토리 패턴 (OAuth2)** | `OAuth2UserServiceFactory` + `@OAuth2ServiceType`으로 프로바이더 라우팅 |
| **통합 응답 래퍼** | `ResponseService` + `CommonResult`, `SingleResult`, `ListResult` |
| **QueryDSL 복잡 쿼리** | `STRING_AGG`로 태그 집계 최적화 |
| **JWT + Redis 전략** | 액세스 토큰 인증, Redis로 리프레시 토큰 및 블랙리스트 관리 |
| **Caffeine 캐싱** | `@Cacheable`로 게시글 목록 쿼리 메서드 레벨 캐싱 |
| **비동기 처리** | 스레드 풀 설정으로 S3 삭제 Non-blocking 처리 |
| **Blue-Green 배포** | Docker Compose + Health Check로 무중단 배포 |
| **글로벌 예외 처리** | `BusinessException` 계층으로 중앙 집중 예외 처리 |

---

## 12. CI/CD 파이프라인

GitHub Actions 워크플로우 구성:

1. **Test Stage**: `./gradlew test --parallel` 실행
2. **Build Stage**: JAR 생성 및 Docker 이미지 빌드
3. **Deploy Stage**: EC2 Blue-Green 배포 + Health Check

---

## 13. 모니터링 및 문서화

| 항목 | URL |
|------|-----|
| **Swagger UI** | `http://localhost:8080/swagger-ui/index.html` |
| **H2 Console** (dev) | `http://localhost:8080/h2-console` |
| **Health Check** | `http://localhost:8080/actuator/health` |
| **Sentry** | 에러 추적 및 모니터링 |

---

## 14. 환경 변수

| 변수명 | 설명 |
|--------|------|
| `DB_URL` | PostgreSQL 데이터베이스 URL |
| `DB_USERNAME` | 데이터베이스 사용자명 |
| `DB_PASSWORD` | 데이터베이스 비밀번호 |
| `JWT_ACCESS_SECRET` | JWT 액세스 토큰 시크릿 |
| `JWT_REFRESH_SECRET` | JWT 리프레시 토큰 시크릿 |
| `REDIS_HOST` | Redis 호스트 |
| `REDIS_PASSWORD` | Redis 비밀번호 |
| `AWS_ACCESS_KEY` | AWS 액세스 키 |
| `AWS_SECRET_KEY` | AWS 시크릿 키 |
| `OAUTH_GOOGLE_*` | Google OAuth 설정 |
| `OAUTH_KAKAO_*` | Kakao OAuth 설정 |
| `OAUTH_NAVER_*` | Naver OAuth 설정 |
| `SENTRY_DSN` | Sentry DSN |
