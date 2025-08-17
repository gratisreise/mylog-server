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
        Page<ArticleResponse> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(memberReader.getById(1L)).thenReturn(testMember);
        when(articleRepository.findMineByMember(testMember, pageable)).thenReturn(emptyPage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(pageable, customUser);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertTrue(result.getContent().isEmpty());

        verify(memberReader).getById(1L);
        verify(articleRepository).findMineByMember(testMember, pageable);
    }

    @Test
    @DisplayName("키워드로 사용자 아티클 검색 성공")
    void getArticles_withKeyword_성공() {
        // Given
        String keyword = "test";
        List<ArticleResponse> articles = List.of(new ArticleResponse(testArticle, List.of()));
        Page<ArticleResponse> articlePage = new PageImpl<>(articles, pageable, 1);

        when(memberReader.getById(1L)).thenReturn(testMember);
        when(articleRepository.searchMineByTitle(testMember, keyword, pageable))
            .thenReturn(articlePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(pageable, customUser, keyword);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Article", result.getContent().get(0).title());

        verify(memberReader).getById(1L);
        verify(articleRepository).searchMineByTitle(testMember, keyword, pageable);
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
        Page<ArticleResponse> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(memberReader.getById(1L)).thenReturn(testMember);
        when(articleRepository.searchMineByTitle(testMember, keyword, pageable))
            .thenReturn(emptyPage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(pageable, customUser, keyword);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(memberReader).getById(1L);
        verify(articleRepository).searchMineByTitle(testMember, keyword, pageable);
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
    @DisplayName("키워드로 전체 아티클 검색 성공")
    void getArticles_withKeywordSearch_성공() {
        // Given
        String keyword = "spring";
        String tag = null;
        List<ArticleResponse> articles = List.of(new ArticleResponse(testArticle, List.of()));
        Page<ArticleResponse> articlePage = new PageImpl<>(articles, pageable, 1);

        when(articleRepository.searchAllByTitle(keyword, pageable)).thenReturn(articlePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(keyword, tag, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Article", result.getContent().get(0).title());

        verify(articleRepository).searchAllByTitle(keyword, pageable);
        verify(articleRepository, never()).searchAllByTagName(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("빈 키워드로 전체 아티클 검색 - 태그 검색으로 전환")
    void getArticles_withEmptyKeyword_태그검색_성공() {
        // Given
        String keyword = "";
        String tag = "java";

        List<ArticleResponse> articles = List.of(new ArticleResponse(testArticle, List.of(tag)));
        Page<ArticleResponse> articlePage = new PageImpl<>(articles, pageable, 1);

        when(articleRepository.searchAllByTagName(tag, pageable)).thenReturn(articlePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(keyword, tag, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(articleRepository, never()).searchAllByTitle(anyString(), any(Pageable.class));
        verify(articleRepository).searchAllByTagName(tag, pageable);
    }

    @Test
    @DisplayName("null 키워드로 전체 아티클 검색 - 태그 검색으로 전환")
    void getArticles_withNullKeyword_태그검색_성공() {
        // Given
        String keyword = null;
        String tag = "spring";

        List<ArticleResponse> articles = List.of(new ArticleResponse(testArticle, List.of(tag)));
        Page<ArticleResponse> articlePage = new PageImpl<>(articles, pageable, 1);

        when(articleRepository.searchAllByTagName(tag, pageable)).thenReturn(articlePage);

        // When
        Page<ArticleResponse> result = articleReader.getArticles(keyword, tag, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(articleRepository, never()).searchAllByTitle(anyString(), any(Pageable.class));
        verify(articleRepository).searchAllByTagName(tag, pageable);
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
}