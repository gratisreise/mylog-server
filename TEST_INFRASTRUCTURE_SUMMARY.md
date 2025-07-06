# 테스트 인프라 요약

## 개요
MyLog Spring Boot 애플리케이션의 서비스 계층 테스트를 위한 포괄적인 테스트 인프라를 구축했습니다. 이 인프라는 유지보수 가능하고 철저한 단위 테스트 작성을 위해 일관되고 재사용 가능한 구성 요소를 제공합니다.

## 생성된 파일

### 핵심 인프라 파일
1.  **`src/test/java/com/mylog/service/ServiceTestBase.java`** - 서비스 테스트를 위한 추상 기본 클래스
2.  **`src/test/java/com/mylog/service/TestDataFactory.java`** - 테스트 데이터 생성을 위한 정적 팩토리 메서드
3.  **`src/test/java/com/mylog/service/TestUtils.java`** - 공통 테스트 작업을 위한 유틸리티 메서드
4.  **`src/test/resources/application-test.yml`** - 테스트 전용 Spring 설정

### 문서 파일
5.  **`src/test/java/com/mylog/service/README.md`** - 테스트 인프라에 대한 포괄적인 문서
6.  **`src/test/java/com/mylog/service/TestInfrastructureUnitTest.java`** - 사용법을 보여주는 예제 단위 테스트

## 주요 특징

### ServiceTestBase.java
-   **Mock 설정**: 모든 리포지토리, 서비스 및 유틸리티에 대해 사전 구성된 Mock 객체
-   **테스트 데이터**: 자동으로 초기화된 테스트 엔티티 (Member, Article, Category, Comment 등)
-   **빌더 메서드**: 사용자 정의 테스트 엔티티 생성을 위한 헬퍼 메서드
-   **단언 헬퍼**: 엔티티 비교 메서드
-   **Spring Boot 통합**: 서비스 테스트를 위한 적절한 어노테이션 및 설정

**주요 Mock Bean:**
-   모든 리포지토리 인터페이스 (MemberRepository, ArticleRepository 등)
-   PasswordEncoder, JwtUtil, S3Service
-   사전 구성된 공통 Mock 동작

### TestDataFactory.java
-   **일관된 테스트 데이터**: 모든 테스트 시나리오에 대한 사전 정의된 상수
-   **엔티티 팩토리**: 모든 도메인 엔티티를 생성하는 메서드
-   **DTO 팩토리**: 모든 요청/응답 DTO를 생성하는 메서드
-   **컬렉션 지원**: 엔티티 목록을 일괄 생성하는 메서드
-   **OAuth 지원**: 다양한 인증 제공자 지원

**사용 가능한 팩토리:**
-   회원 (로컬, Google, Kakao, Naver OAuth)
-   카테고리 및 태그가 포함된 게시글
-   답글 지원이 포함된 댓글
-   카테고리 및 태그
-   알림
-   모든 요청 DTO (SignUp, Login, ArticleCreate 등)

### TestUtils.java
-   **보안 컨텍스트 관리**: 인증 테스트를 위한 설정/해제
-   **페이지네이션 지원**: 페이지 생성 및 검증 유틸리티
-   **단언 헬퍼**: 의미 있는 오류 메시지를 포함한 상세한 엔티티 비교
-   **결과 검증**: CommonResult, SingleResult, ListResult 검증
-   **예외 테스트**: 예외 시나리오 테스트를 위한 헬퍼
-   **타입-세이프 검증**: ID, 문자열, 목록 및 감사 필드 검증

### 테스트 설정 (application-test.yml)
-   **H2 인메모리 데이터베이스**: PostgreSQL 호환성을 갖춘 빠른 테스트 실행
-   **Mock 서비스**: 외부 서비스 비활성화 (Sentry, 실제 AWS S3)
-   **테스트 JWT 키**: 안전한 테스트 전용 JWT 설정
-   **디버그 로깅**: 테스트에 적합한 로깅 수준
-   **Redis 설정**: 테스트 준비된 Redis 설정

## 사용 패턴

### 기본 서비스 테스트
```java
class YourServiceTest extends ServiceTestBase {
    @InjectMocks
    private YourService yourService;

    @Test
    void testMethod_success() {
        // Given - 상속된 테스트 데이터 사용
        Member member = testMember;

        // When
        YourResult result = yourService.method(member.getId());

        // Then
        TestUtils.assertSingleResultSuccess(result);
        verify(memberRepository, times(1)).findById(member.getId());
    }
}
```

### 사용자 정의 테스트 데이터 생성
```java
// TestDataFactory 사용
Member member = TestDataFactory.createTestMember();
List<Article> articles = TestDataFactory.createTestArticles(member, category, 5);

// ServiceTestBase 빌더 사용
Member customMember = createMemberBuilder()
    .email("custom@test.com")
    .memberName("Custom User")
    .build();
```

### 페이지네이션 테스트
```java
// 페이지네이션된 데이터 생성
List<Member> members = TestDataFactory.createTestMembers(25);
Page<Member> page = TestUtils.createPage(members, 0, 10);

// 페이지 속성 검증
TestUtils.assertPageProperties(page, 10, 25, 0);
```

## 테스트 표준

### 테스트 이름 규칙
-   **한글 Display 이름**: `@DisplayName("회원가입 성공")`
-   **메서드 이름**: `signUp_성공()`, `getMember_실패_데이터없음()`

### 테스트 구조
-   **Given-When-Then**: 명확한 테스트 구성
-   **서술적 단언**: 의미 있는 오류 메시지를 위해 TestUtils 헬퍼 사용
-   **Mock 검증**: 중요한 리포지토리/서비스 상호작용 검증

### 커버리지 요구사항
-   **80% 라인 커버리지**: 서비스 클래스에 대해 JaCoCo로 강제
-   **포괄적인 엣지 케이스**: 성공 및 실패 시나리오 모두 테스트
-   **예외 테스트**: TestUtils 헬퍼로 오류 처리 검증

## 컴파일 및 테스트 상태

✅ **모든 파일이 성공적으로 컴파일됩니다**
✅ **단위 테스트 통과** (TestInfrastructureUnitTest의 11/11 테스트)
✅ **컴파일 오류 없음**
✅ **JaCoCo 통합 작동 중**

## 다음 단계

1.  **서비스 테스트 생성**: 실제 서비스 테스트 클래스를 위해 `ServiceTestBase` 확장
2.  **통합 테스트**: 리포지토리 통합 테스트를 위해 인프라 사용
3.  **성능 테스트**: 부하 테스트 시나리오를 위해 `TestDataFactory` 활용
4.  **사용자 정의 검증기**: `TestUtils`에 도메인별 단언 메서드 추가

## 명령어

```bash
# 테스트 인프라 컴파일
./gradlew compileTestJava

# 인프라 단위 테스트 실행


# 커버리지 보고서 생성
./gradlew jacocoTestReport

# 커버리지 요구사항 검증
./gradlew jacocoTestCoverageVerification
```

## 파일 위치 요약

```
src/test/java/com/mylog/service/
├── ServiceTestBase.java              # 추상 기본 클래스
├── TestDataFactory.java              # 테스트 데이터 생성
├── TestUtils.java                     # 테스트 유틸리티
├── TestInfrastructureUnitTest.java    # 예제/검증 테스트
└── README.md                         # 상세 문서

src/test/resources/
└── application-test.yml               # 테스트 설정
```

테스트 인프라는 사용 준비가 되었으며, MyLog 애플리케이션의 포괄적인 서비스 계층 테스트를 위한 견고한 기반을 제공합니다.
