package com.mylog.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mylog.entity.Article;
import com.mylog.entity.ArticleTag;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.entity.Tag;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.ArticleTagRepository;
import com.mylog.repository.TagRepository;
import com.mylog.service.TagService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @InjectMocks
    private TagService tagService;

    @Mock
    private ArticleTagRepository articleTagRepository;
    @Mock
    private TagRepository tagRepository;

    private Article testArticle;
    private Member testMember;
    private Category testCategory;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        // Member 설정
        testMember = Member.builder()
            .id(1L)
            .memberName("테스트유저")
            .nickname("테스트닉네임")
            .build();

        // Category 설정
        testCategory = Category.builder()
            .id(1L)
            .categoryName("테스트카테고리")
            .member(testMember)
            .build();

        // Tag 설정
        testTag = Tag.builder()
            .id(1L)
            .tagName("테스트태그")
            .build();

        // Article 설정
        testArticle = Article.builder()
            .id(1L)
            .title("테스트제목")
            .content("테스트내용")
            .member(testMember)
            .category(testCategory)
            .build();

        // ArticleTag 설정
        ArticleTag testArticleTag = new ArticleTag(testArticle, testTag);

        // Pageable 설정
        Pageable pageable = PageRequest.of(0, 10);
    }

    @Test
    void 태그_저장_성공_새로운태그() {
        // given
        List<String> tags = Arrays.asList("새로운태그");

        when(tagRepository.existsByTagName("새로운태그")).thenReturn(false);
        when(tagRepository.save(any(Tag.class)))
            .thenReturn(Tag.builder().id(1L).tagName("새로운태그").build());
        when(tagRepository.findByTagName("새로운태그"))
            .thenReturn(Optional.of(Tag.builder().id(1L).tagName("새로운태그").build()));
        when(articleTagRepository.save(any(ArticleTag.class)))
            .thenReturn(new ArticleTag(testArticle, testTag));

        // when
        tagService.saveTag(tags, testArticle);

        // then
        verify(tagRepository).existsByTagName("새로운태그");
        verify(tagRepository).save(any(Tag.class));
        verify(tagRepository).findByTagName("새로운태그");
        verify(articleTagRepository).save(any(ArticleTag.class));
    }

    @Test
    void 태그_저장_성공_기존태그() {
        // given
        List<String> tags = Arrays.asList("기존태그");

        when(tagRepository.existsByTagName("기존태그")).thenReturn(true);
        when(tagRepository.findByTagName("기존태그"))
            .thenReturn(Optional.of(Tag.builder().id(1L).tagName("기존태그").build()));
        when(articleTagRepository.save(any(ArticleTag.class)))
            .thenReturn(new ArticleTag(testArticle, testTag));

        // when
        tagService.saveTag(tags, testArticle);

        // then
        verify(tagRepository).existsByTagName("기존태그");
        verify(tagRepository, never()).save(any(Tag.class));
        verify(tagRepository).findByTagName("기존태그");
        verify(articleTagRepository).save(any(ArticleTag.class));
    }

    @Test
    void 태그_저장_실패_태그조회실패() {
        // given
        List<String> tags = Arrays.asList("존재하지않는태그");

        when(tagRepository.existsByTagName("존재하지않는태그")).thenReturn(true);
        when(tagRepository.findByTagName("존재하지않는태그"))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tagService.saveTag(tags, testArticle))
            .isInstanceOf(CMissingDataException.class);

        verify(tagRepository).existsByTagName("존재하지않는태그");
        verify(tagRepository, never()).save(any(Tag.class));
        verify(tagRepository).findByTagName("존재하지않는태그");
        verify(articleTagRepository, never()).save(any(ArticleTag.class));
    }


}