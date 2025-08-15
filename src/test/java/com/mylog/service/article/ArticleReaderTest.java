package com.mylog.service.article;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.article.ArticleResponse;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Article;
import com.mylog.model.entity.Category;
import com.mylog.model.entity.Member;
import com.mylog.repository.article.ArticleRepository;
import com.mylog.service.member.MemberReader;
import com.mylog.service.tag.TagReader;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class ArticleReaderTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private TagReader tagReader;

    @Mock
    private MemberReader memberReader;

    @InjectMocks
    private ArticleReader articleReader;


    private CustomUser customUser;
    private Pageable pageable;
    private Member testMember;
    private Article testArticle;
    private com.mylog.model.entity.Category testCategory;

    @BeforeEach
    void setUp() {
        // Create test entities
        testMember = Member.builder()
            .id(1L)
            .email("test@example.com")
            .password("password123")
            .memberName("Test User")
            .nickname("testuser")
            .bio("Test bio")
            .profileImg("https://example.com/default.jpg")
            .provider(OauthProvider.LOCAL)
            .providerId("test@example.com" + OauthProvider.LOCAL)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        testCategory = com.mylog.model.entity.Category.builder()
            .id(1L)
            .categoryName("Test Category")
            .member(testMember)
            .createdAt(LocalDateTime.now().toLocalDate())
            .updatedAt(LocalDateTime.now().toLocalDate())
            .build();

        testArticle = Article.builder()
            .id(1L)
            .title("Test Article")
            .content("This is test article content")
            .articleImg("https://example.com/article.jpg")
            .member(testMember)
            .category(testCategory)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        customUser = new CustomUser(testMember, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        pageable = Pageable.ofSize(20);
    }

    private List<Article> createTestArticles(Member member, Category category, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> Article.builder()
                        .id((long) (i + 1))
                        .title("Test Article " + i)
                        .content("Test content " + i)
                        .articleImg("https://example.com/article" + i + ".jpg")
                        .member(member)
                        .category(category)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build())
                .toList();
    }

    private Member.MemberBuilder createMemberBuilder() {
        return Member.builder()
                .email("test@example.com")
                .password("password123")
                .memberName("Test User")
                .nickname("testuser")
                .bio("Test bio")
                .profileImg("https://example.com/default.jpg")
                .provider(OauthProvider.LOCAL)
                .providerId("test@example.com" + OauthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("사용자 아티클 목록 조회 성공")
    void getArticles_withCustomUser_성공() {
        // Given
        List<Article> articles = createTestArticles(testMember, testCategory, 3);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, 3);

        when(memberReader.getById(1L)).thenReturn(testMember);
        when(articleRepository.findAllByMember(testMember, pageable)).thenReturn(articlePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(pageable, customUser);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getTotalPages());

        List<ArticleResponse> content = result.getContent();
        assertEquals(3, content.size());
        assertEquals("Test Article 0", content.get(0).title());
        assertEquals("testuser", content.get(0).author());
        assertEquals("Test Category", content.get(0).category());

        verify(memberReader).getById(1L);
        verify(articleRepository).findAllByMember(testMember, pageable);
    }

    @Test
    @DisplayName("사용자 아티클 목록 조회 - 멤버 없음 예외")
    void getArticles_memberNotFound_예외발생() {
        // Given
        when(memberReader.getById(1L)).thenThrow(CMissingDataException.class);

        // When & Then
        assertThrows(CMissingDataException.class, () ->
            articleReader.getArticles(pageable, customUser)
        );

        verify(memberReader).getById(1L);
        verify(articleRepository, never()).findAllByMember(any(Member.class), any(Pageable.class));
    }

    @Test
    @DisplayName("사용자 아티클 목록 조회 - 빈 결과")
    void getArticles_emptyResult_성공() {
        // Given
        Page<Article> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(memberReader.getById(1L)).thenReturn(testMember);
        when(articleRepository.findAllByMember(testMember, pageable)).thenReturn(emptyPage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(pageable, customUser);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertTrue(result.getContent().isEmpty());

        verify(memberReader).getById(1L);
        verify(articleRepository).findAllByMember(testMember, pageable);
    }

    @Test
    @DisplayName("키워드로 사용자 아티클 검색 성공")
    void getArticles_withKeyword_성공() {
        // Given
        String keyword = "test";
        List<Article> articles = List.of(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, 1);

        when(memberReader.getById(1L)).thenReturn(testMember);
        when(articleRepository.findByMemberAndTitleContainingIgnoreCase(testMember, keyword, pageable))
            .thenReturn(articlePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(pageable, customUser, keyword);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Article", result.getContent().get(0).title());

        verify(memberReader).getById(1L);
        verify(articleRepository).findByMemberAndTitleContainingIgnoreCase(testMember, keyword, pageable);
    }

    @Test
    @DisplayName("키워드로 사용자 회원없음 예외발생")
    void getArticles_withKeyword_memberNotFound_예외발생() {
        // Given
        String keyword = "test";
        when(memberReader.getById(1L)).thenThrow(CMissingDataException.class);

        // When & Then
        assertThatThrownBy(()-> articleReader.getArticles(pageable, customUser, keyword))
            .isInstanceOf(CMissingDataException.class);

        verify(memberReader).getById(1L);
        verify(articleRepository, never()).findByMemberAndTitleContainingIgnoreCase(any(Member.class), anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("키워드로 사용자 아티클 검색 - 검색 결과 없음")
    void getArticles_withKeyword_noResults_성공() {
        // Given
        String keyword = "nonexistent";
        Page<Article> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(memberReader.getById(1L)).thenReturn(testMember);
        when(articleRepository.findByMemberAndTitleContainingIgnoreCase(testMember, keyword, pageable))
            .thenReturn(emptyPage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(pageable, customUser, keyword);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(memberReader).getById(1L);
        verify(articleRepository).findByMemberAndTitleContainingIgnoreCase(testMember, keyword, pageable);
    }

    @Test
    @DisplayName("아티클 상세 조회 성공")
    void getArticle_성공() {
        // Given
        Long articleId = 1L;
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(testArticle));

        // When
        ArticleResponse result = articleReader.getArticle(articleId);

        // Then
        assertNotNull(result);
        assertEquals(testArticle.getId(), result.id());
        assertEquals(testArticle.getTitle(), result.title());
        assertEquals(testArticle.getContent(), result.content());
        assertEquals(testMember.getNickname(), result.author());
        assertEquals(testCategory.getCategoryName(), result.category());

        verify(articleRepository).findById(articleId);
    }

    @Test
    @DisplayName("아티클 상세 조회 - 아티클 없음 예외")
    void getArticle_articleNotFound_예외발생() {
        // Given
        Long articleId = 999L;
        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CMissingDataException.class, () ->
            articleReader.getArticle(articleId)
        );

        verify(articleRepository).findById(articleId);
    }

    @Test
    @DisplayName("전체 아티클 목록 조회 성공")
    void getArticles_allArticles_성공() {
        // Given
        List<Article> articles = createTestArticles(testMember, testCategory, 5);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, 5);

        when(articleRepository.findAll(pageable)).thenReturn(articlePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(pageable);

        // Then
        assertNotNull(result);
        assertEquals(5, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(5, result.getContent().size());

        verify(articleRepository).findAll(pageable);
    }

    @Test
    @DisplayName("전체 아티클 목록 조회 - 빈 결과")
    void getArticles_allArticles_emptyResult_성공() {
        // Given
        Page<Article> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(articleRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(pageable);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());

        verify(articleRepository).findAll(pageable);
    }

    @Test
    @DisplayName("키워드로 전체 아티클 검색 성공")
    void getArticles_withKeywordSearch_성공() {
        // Given
        String keyword = "spring";
        String tag = null;
        List<Article> articles = List.of(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, 1);

        when(articleRepository.findByTitleContainingIgnoreCase(keyword, pageable))
            .thenReturn(articlePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(keyword, tag, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Article", result.getContent().get(0).title());

        verify(articleRepository).findByTitleContainingIgnoreCase(keyword, pageable);
        verify(articleRepository, never()).findAllByTagName(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("빈 키워드로 전체 아티클 검색 - 태그 검색으로 전환")
    void getArticles_withEmptyKeyword_태그검색_성공() {
        // Given
        String keyword = "";
        String tag = "java";
        List<Article> articles = List.of(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, 1);

        when(articleRepository.findAllByTagName(tag, pageable)).thenReturn(articlePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(keyword, tag, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(articleRepository, never()).findByTitleContainingIgnoreCase(anyString(), any(Pageable.class));
        verify(articleRepository).findAllByTagName(tag, pageable);
    }

    @Test
    @DisplayName("null 키워드로 전체 아티클 검색 - 태그 검색으로 전환")
    void getArticles_withNullKeyword_태그검색_성공() {
        // Given
        String keyword = null;
        String tag = "spring";
        List<Article> articles = List.of(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, 1);

        when(articleRepository.findAllByTagName(tag, pageable)).thenReturn(articlePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(keyword, tag, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(articleRepository, never()).findByTitleContainingIgnoreCase(anyString(), any(Pageable.class));
        verify(articleRepository).findAllByTagName(tag, pageable);
    }

    @Test
    @DisplayName("키워드와 태그 모두 제공 - 키워드 우선 검색")
    void getArticles_withKeywordAndTag_키워드우선_성공() {
        // Given
        String keyword = "test";
        String tag = "java";
        List<Article> articles = List.of(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, 1);

        when(articleRepository.findByTitleContainingIgnoreCase(keyword, pageable))
            .thenReturn(articlePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(keyword, tag, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(articleRepository).findByTitleContainingIgnoreCase(keyword, pageable);
        verify(articleRepository, never()).findAllByTagName(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("getArticleById 성공")
    void getArticleById_성공() {
        // Given
        Long articleId = 1L;
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(testArticle));

        // When
        Article result = articleReader.getArticleById(articleId);

        // Then
        assertNotNull(result);
        assertEquals(testArticle.getId(), result.getId());
        assertEquals(testArticle.getTitle(), result.getTitle());
        assertEquals(testArticle.getContent(), result.getContent());

        verify(articleRepository).findById(articleId);
    }

    @Test
    @DisplayName("getArticleById - 아티클 없음 예외")
    void getArticleById_articleNotFound_예외발생() {
        // Given
        Long articleId = 999L;
        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CMissingDataException.class, () ->
            articleReader.getArticleById(articleId)
        );

        verify(articleRepository).findById(articleId);
    }

    @Test
    @DisplayName("페이지네이션 테스트 - 다양한 페이지 크기")
    void getArticles_pagination_다양한페이지크기() {
        // Given
        Pageable smallPage = Pageable.ofSize(5);
        List<Article> articles = createTestArticles(testMember, testCategory, 10);
        Page<Article> articlePage = new PageImpl<>(articles.subList(0, 5), smallPage, 10);

        when(articleRepository.findAll(smallPage)).thenReturn(articlePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(smallPage);

        // Then
        assertNotNull(result);
        assertEquals(10, result.getTotalElements()); // 전체 요소 수
        assertEquals(2, result.getTotalPages()); // 전체 페이지 수
        assertEquals(5, result.getContent().size()); // 현재 페이지 요소 수
        assertTrue(result.hasNext()); // 다음 페이지 존재

        verify(articleRepository).findAll(smallPage);
    }

    @Test
    @DisplayName("대용량 데이터 처리 테스트")
    void getArticles_largeDataSet_성공() {
        // Given
        List<Article> largeArticleList = createTestArticles(testMember, testCategory, 100);
        Page<Article> largePage = new PageImpl<>(largeArticleList.subList(0, 20), pageable, 100);

        when(articleRepository.findAll(pageable)).thenReturn(largePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(pageable);

        // Then
        assertNotNull(result);
        assertEquals(100, result.getTotalElements());
        assertEquals(5, result.getTotalPages());
        assertEquals(20, result.getContent().size());

        verify(articleRepository).findAll(pageable);
    }


    @Test
    @DisplayName("검색 조건별 분기 테스트 - isClear 메서드 검증")
    void getArticles_searchConditions_분기테스트() {
        // Given - 키워드가 공백인 경우
        String emptyKeyword = "  ";
        String tag = "testtag";
        List<Article> articles = List.of(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, 1);

        // 공백도 empty가 아니므로 키워드 검색이 실행되어야 함
        when(articleRepository.findByTitleContainingIgnoreCase(emptyKeyword, pageable))
            .thenReturn(articlePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(emptyKeyword, tag, pageable);

        // Then
        assertNotNull(result);
        verify(articleRepository).findByTitleContainingIgnoreCase(emptyKeyword, pageable);
        verify(articleRepository, never()).findAllByTagName(anyString(), any(Pageable.class));
    }
}