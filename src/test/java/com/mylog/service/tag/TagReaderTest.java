package com.mylog.service.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.exception.CMissingDataException;
import com.mylog.model.entity.Article;
import com.mylog.model.entity.Tag;
import com.mylog.repository.tag.TagRepository;
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

@ExtendWith(MockitoExtension.class)
class TagReaderTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagReader tagReader;

    private Article testArticle;
    private Tag testTag;

    @BeforeEach
    void setUp() {
        testArticle = new Article();
        testTag = new Tag("Java");
    }

    @Test
    void 태그목록조회_정상반환() {
        // given
        List<String> expectedTags = Arrays.asList("Java", "Spring", "JPA");
        when(tagRepository.findByArticle(testArticle)).thenReturn(expectedTags);

        // when
        List<String> result = tagReader.getTags(testArticle);

        // then
        assertThat(result).isEqualTo(expectedTags);
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("Java", "Spring", "JPA");
        verify(tagRepository, times(1)).findByArticle(testArticle);
    }

    @Test
    void 태그목록조회_태그없음_빈리스트반환() {
        // given
        when(tagRepository.findByArticle(testArticle)).thenReturn(Collections.emptyList());

        // when
        List<String> result = tagReader.getTags(testArticle);

        // then
        assertThat(result).isEmpty();
        verify(tagRepository, times(1)).findByArticle(testArticle);
    }

    @Test
    void getTagByTagName_존재하는태그_태그반환() {
        // given
        String tagName = "Java";
        when(tagRepository.findByTagName(tagName)).thenReturn(Optional.of(testTag));

        // when
        Tag result = tagReader.getTagByTagName(tagName);

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testTag);
        verify(tagRepository, times(1)).findByTagName(tagName);
    }

    @Test
    void getTagByTagName_존재하지않는태그_예외발생() {
        // given
        String tagName = "NonExisting";
        when(tagRepository.findByTagName(tagName)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tagReader.getTagByTagName(tagName))
            .isInstanceOf(CMissingDataException.class);

        verify(tagRepository, times(1)).findByTagName(tagName);
    }

    @Test
    void getTagByTagName_빈문자열_예외발생() {
        // given
        String emptyTagName = "";
        when(tagRepository.findByTagName(emptyTagName)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tagReader.getTagByTagName(emptyTagName))
            .isInstanceOf(CMissingDataException.class);

        verify(tagRepository, times(1)).findByTagName(emptyTagName);
    }
}