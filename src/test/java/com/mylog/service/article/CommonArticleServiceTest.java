package com.mylog.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.mylog.dto.article.ArticleResponse;
import com.mylog.entity.Article;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CategoryRepository;
import com.mylog.service.article.CommonArticleService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommonArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CommonArticleService commonArticleService;

    @Test
    void getArticle_정상적인_호출() {
        // given
        Long articleId = 1L;
        Article article = Article.builder()
            .id(articleId)
            .title("Test Article")
            .content("This is a test article.")
            .createdAt(LocalDateTime.now())
            .category(Category.builder().categoryName("Technology").build())
            .member(Member.builder().nickname("Author").build())
            .build();

        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));

        // when
        ArticleResponse response = commonArticleService.getArticle(articleId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Article");
        assertThat(response.getContent()).isEqualTo("This is a test article.");
        assertThat(response.getCategory()).isEqualTo("Technology");
        assertThat(response.getAuthor()).isEqualTo("Author");
    }

    @Test
    void getArticle_존재하지_않는_기사() {
        // given
        Long articleId = 1L;
        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commonArticleService.getArticle(articleId))
            .isInstanceOf(CMissingDataException.class);
    }
}