package com.mylog.service.article;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.mylog.dto.article.ArticleResponse;
import com.mylog.entity.Article;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CategoryRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CommonArticleServiceTest {

    @InjectMocks
    private CommonArticleService articleService;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private Member testMember;
    private Category testCategory;
    private Article testArticle;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .id(1L)
            .memberName("테스트유저")
            .nickname("테스트닉네임")
            .email("test@test.com")
            .build();

        testCategory = Category.builder()
            .id(1L)
            .categoryName("테스트카테고리")
            .build();

        testArticle = Article.builder()
            .id(1L)
            .title("테스트제목")
            .content("테스트내용")
            .member(testMember)
            .category(testCategory)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void 게시글_조회_성공() {
        // given
        Long articleId = 1L;

        when(articleRepository.findById(articleId))
            .thenReturn(Optional.of(testArticle));

        // when
        ArticleResponse result = articleService.getArticle(articleId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("테스트제목");
        assertThat(result.getContent()).isEqualTo("테스트내용");
        assertThat(result.getAuthor()).isEqualTo("테스트닉네임");
        assertThat(result.getCategory()).isEqualTo("테스트카테고리");

        verify(articleRepository).findById(articleId);
    }

    @Test
    void 게시글_조회_실패_게시글없음() {
        // given
        Long articleId = 999L;

        when(articleRepository.findById(articleId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> articleService.getArticle(articleId))
            .isInstanceOf(CMissingDataException.class);

        verify(articleRepository).findById(articleId);
    }


    @Test
    void 전체_게시글_조회_성공() {
        // given
        List<Article> articles = Collections.singletonList(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        when(articleRepository.findAll(pageable)).thenReturn(articlePage);

        // when
        Page<ArticleResponse> result = articleService.getArticles(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0))
            .satisfies(response -> {
                assertThat(response.getTitle()).isEqualTo("테스트제목");
                assertThat(response.getContent()).isEqualTo("테스트내용");
                assertThat(response.getAuthor()).isEqualTo("테스트닉네임");
                assertThat(response.getCategory()).isEqualTo("테스트카테고리");
            });

        verify(articleRepository).findAll(pageable);
    }

    @Test
    void 전체_게시글_조회_성공_게시글없음() {
        // given
        Page<Article> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(articleRepository.findAll(pageable))
            .thenReturn(emptyPage);

        // when
        Page<ArticleResponse> result = articleService.getArticles(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(articleRepository).findAll(pageable);
    }


    @Test
    void 게시글_검색_성공() {
        // given
        String keyword = "테스트";
        List<Article> articles = Collections.singletonList(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        when(articleRepository.findByTitleContainingIgnoreCase(keyword, pageable))
            .thenReturn(articlePage);

        // when
        Page<ArticleResponse> result = articleService.getArticles(keyword, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0))
            .satisfies(response -> {
                assertThat(response.getTitle()).isEqualTo("테스트제목");
                assertThat(response.getContent()).isEqualTo("테스트내용");
                assertThat(response.getAuthor()).isEqualTo("테스트닉네임");
                assertThat(response.getCategory()).isEqualTo("테스트카테고리");
            });

        verify(articleRepository).findByTitleContainingIgnoreCase(keyword, pageable);
    }

    @Test
    void 게시글_검색_성공_결과없음() {
        // given
        String keyword = "존재하지않는키워드";
        Page<Article> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(articleRepository.findByTitleContainingIgnoreCase(keyword, pageable))
            .thenReturn(emptyPage);

        // when
        Page<ArticleResponse> result = articleService.getArticles(keyword, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(articleRepository).findByTitleContainingIgnoreCase(keyword, pageable);
    }

    @Test
    void 태그_이름으로_게시글_검색_성공() {
        // given
        String tagName = "테스트태그";
        List<Article> articles = Collections.singletonList(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        when(articleRepository.findAllByTagName(tagName, pageable)).thenReturn(articlePage);

        // when
        Page<ArticleResponse> result = articleService.getArticlesByTagName(tagName, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0))
            .satisfies(response -> {
                assertThat(response.getTitle()).isEqualTo("테스트제목");
                assertThat(response.getContent()).isEqualTo("테스트내용");
                assertThat(response.getAuthor()).isEqualTo("테스트닉네임");
                assertThat(response.getCategory()).isEqualTo("테스트카테고리");
            });

        verify(articleRepository).findAllByTagName(tagName, pageable);
    }

    @Test
    void 태그_이름으로_게시글_검색_성공_결과없음() {
        // given
        String tagName = "존재하지않는태그";
        Page<Article> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(articleRepository.findAllByTagName(tagName, pageable))
            .thenReturn(emptyPage);

        // when
        Page<ArticleResponse> result = articleService.getArticlesByTagName(tagName, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(articleRepository).findAllByTagName(tagName, pageable);
    }

}