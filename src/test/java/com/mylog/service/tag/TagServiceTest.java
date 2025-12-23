package com.mylog.service.tag;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.api.tag.TagReader;
import com.mylog.api.tag.TagService;
import com.mylog.domain.entity.Article;
import com.mylog.domain.entity.ArticleTag;
import com.mylog.domain.entity.Tag;
import com.mylog.repository.tag.TagRepository;
import com.mylog.service.articletag.ArticleTagWriter;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagReader tagReader;

    @Mock
    private ArticleTagWriter articleTagWriter;

    @InjectMocks
    private TagService tagService;

    private Article testArticle;
    private List<String> testTags;

    @BeforeEach
    void setUp() {
        testArticle = new Article();
        testTags = Arrays.asList("Java", "Spring", "JPA");
    }

    @Test
    void 태그저장_모두없는태그명_저장() {
        // given
        when(tagRepository.existsByTagName(anyString())).thenReturn(false);

        Tag javaTag = new Tag("Java");
        Tag springTag = new Tag("Spring");
        Tag jpaTag = new Tag("JPA");

        when(tagReader.getTagByTagName("Java")).thenReturn(javaTag);
        when(tagReader.getTagByTagName("Spring")).thenReturn(springTag);
        when(tagReader.getTagByTagName("JPA")).thenReturn(jpaTag);

        // when
        tagService.saveTag(testTags, testArticle);

        // then
        verify(tagRepository, times(3)).existsByTagName(anyString());
        verify(tagRepository, times(3)).save(any(Tag.class));
        verify(tagReader, times(3)).getTagByTagName(anyString());
        verify(articleTagWriter, times(3)).crateArticleTag(any(ArticleTag.class));
    }

    @Test
    void 태그저장_존재하는태그_저장안함() {
        // given
        when(tagRepository.existsByTagName(anyString())).thenReturn(true);

        Tag javaTag = new Tag("Java");
        Tag springTag = new Tag("Spring");
        Tag jpaTag = new Tag("JPA");

        when(tagReader.getTagByTagName("Java")).thenReturn(javaTag);
        when(tagReader.getTagByTagName("Spring")).thenReturn(springTag);
        when(tagReader.getTagByTagName("JPA")).thenReturn(jpaTag);

        // when
        tagService.saveTag(testTags, testArticle);

        // then
        verify(tagRepository, times(3)).existsByTagName(anyString());
        verify(tagRepository, never()).save(any(Tag.class));
        verify(tagReader, times(3)).getTagByTagName(anyString());
        verify(articleTagWriter, times(3)).crateArticleTag(any(ArticleTag.class));
    }

    @Test
    void 태그저장_존재유무복합_불리언값에따라결정() {
        // given
        when(tagRepository.existsByTagName("Java")).thenReturn(true);
        when(tagRepository.existsByTagName("Spring")).thenReturn(false);
        when(tagRepository.existsByTagName("JPA")).thenReturn(true);

        Tag javaTag = new Tag("Java");
        Tag springTag = new Tag("Spring");
        Tag jpaTag = new Tag("JPA");

        when(tagReader.getTagByTagName("Java")).thenReturn(javaTag);
        when(tagReader.getTagByTagName("Spring")).thenReturn(springTag);
        when(tagReader.getTagByTagName("JPA")).thenReturn(jpaTag);

        // when
        tagService.saveTag(testTags, testArticle);

        // then
        verify(tagRepository, times(3)).existsByTagName(anyString());
        verify(tagRepository, times(1)).save(any(Tag.class)); // Spring만 저장
        verify(tagReader, times(3)).getTagByTagName(anyString());
        verify(articleTagWriter, times(3)).crateArticleTag(any(ArticleTag.class));
    }

    @Test
    void 태그저장_비어있는태그명_로직실행안함() {
        // given
        List<String> emptyTags = Arrays.asList();

        // when
        tagService.saveTag(emptyTags, testArticle);

        // then
        verify(tagRepository, never()).existsByTagName(anyString());
        verify(tagRepository, never()).save(any(Tag.class));
        verify(tagReader, never()).getTagByTagName(anyString());
        verify(articleTagWriter, never()).crateArticleTag(any(ArticleTag.class));
    }
}