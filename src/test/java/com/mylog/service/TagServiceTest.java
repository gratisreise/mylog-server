package com.mylog.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.model.entity.Article;
import com.mylog.model.entity.ArticleTag;
import com.mylog.model.entity.Tag;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.ArticleTagRepository;
import com.mylog.repository.TagRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @InjectMocks
    private TagService tagService;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ArticleTagRepository articleTagRepository;

    private Article article;
    private Tag tag1;
    private Tag tag2;
    private List<String> tags;
    private Long articleId = 1L;
    private String tagName1 = "spring";
    private String tagName2 = "java";

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        article = Article.builder()
            .id(articleId)
            .title("Test Article")
            .content("Test Content")
            .build();

        tag1 = new Tag(tagName1);
        tag2 = new Tag(tagName2);

        tags = List.of(tagName1, tagName2);
    }

    @Test
    void 태그_저장_성공_새태그생성() {
        // Given
        when(tagRepository.existsByTagName(tagName1)).thenReturn(false);
        when(tagRepository.existsByTagName(tagName2)).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(tag1, tag2);
        when(tagRepository.findByTagName(tagName1)).thenReturn(Optional.of(tag1));
        when(tagRepository.findByTagName(tagName2)).thenReturn(Optional.of(tag2));
        when(articleTagRepository.save(any(ArticleTag.class))).thenReturn(
            new ArticleTag(article, tag1), new ArticleTag(article, tag2));

        // When
        tagService.saveTag(tags, article);

        // Then
        verify(tagRepository, times(2)).existsByTagName(anyString());
        verify(tagRepository, times(2)).save(any(Tag.class));
        verify(tagRepository).findByTagName(tagName1);
        verify(tagRepository).findByTagName(tagName2);
        verify(articleTagRepository, times(2)).save(any(ArticleTag.class));
    }

    @Test
    void 태그_저장_성공_기존태그사용() {
        // Given
        when(tagRepository.existsByTagName(tagName1)).thenReturn(true);
        when(tagRepository.existsByTagName(tagName2)).thenReturn(true);
        when(tagRepository.findByTagName(tagName1)).thenReturn(Optional.of(tag1));
        when(tagRepository.findByTagName(tagName2)).thenReturn(Optional.of(tag2));
        when(articleTagRepository.save(any(ArticleTag.class))).thenReturn(
            new ArticleTag(article, tag1), new ArticleTag(article, tag2));

        // When
        tagService.saveTag(tags, article);

        // Then
        verify(tagRepository, times(2)).existsByTagName(anyString());
        verify(tagRepository, never()).save(any(Tag.class));
        verify(tagRepository).findByTagName(tagName1);
        verify(tagRepository).findByTagName(tagName2);
        verify(articleTagRepository, times(2)).save(any(ArticleTag.class));
    }

    @Test
    void 태그_저장_태그조회실패_예외발생() {
        // Given
        when(tagRepository.existsByTagName(tagName1)).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(tag1);
        when(tagRepository.findByTagName(tagName1)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tagService.saveTag(List.of(tagName1), article))
            .isInstanceOf(CMissingDataException.class);
        verify(tagRepository).existsByTagName(tagName1);
        verify(tagRepository).save(any(Tag.class));
        verify(tagRepository).findByTagName(tagName1);
        verify(articleTagRepository, never()).save(any());
    }

    @Test
    void 태그_저장_빈태그목록_저장안함() {
        // Given
        List<String> emptyTags = List.of();

        // When
        tagService.saveTag(emptyTags, article);

        // Then
        verify(tagRepository, never()).existsByTagName(anyString());
        verify(tagRepository, never()).save(any());
        verify(tagRepository, never()).findByTagName(anyString());
        verify(articleTagRepository, never()).save(any());
    }
}