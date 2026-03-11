package com.mylog.domain.article.service;

import static org.mockito.BDDMockito.*;

import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.entity.ArticleTag;
import com.mylog.domain.article.entity.Tag;
import com.mylog.domain.article.repository.ArticleTagRepository;
import com.mylog.domain.article.repository.TagRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagWriter 단위 테스트")
class TagWriterTest {

  @Mock private TagRepository tagRepository;
  @Mock private ArticleTagRepository articleTagRepository;

  @InjectMocks private TagWriter tagWriter;

  @Test
  @DisplayName("성공: AI 태그 저장 - 기존 태그 재사용 및 새 태그 생성")
  void saveAiTags_ReuseExistingTag() {
    // given
    List<String> tagNames = Arrays.asList("Spring", "Java");
    Article article = Article.builder().id(1L).title("제목").content("내용").build();
    Tag existingTag = Tag.from("Spring");
    Tag newTag = Tag.from("Java");

    given(tagRepository.findByTagName("Spring")).willReturn(Optional.of(existingTag));
    given(tagRepository.findByTagName("Java")).willReturn(Optional.empty());
    given(tagRepository.save(any(Tag.class))).willReturn(newTag);
    given(articleTagRepository.save(any(ArticleTag.class))).willAnswer(inv -> inv.getArgument(0));

    // when
    tagWriter.saveAiTags(tagNames, article);

    // then
    then(articleTagRepository).should().deleteByArticle(article);
    then(tagRepository).should().findByTagName("Spring");
    then(tagRepository).should().findByTagName("Java");
    then(tagRepository).should().save(any(Tag.class)); // Java는 새로 생성
    then(articleTagRepository).should(times(2)).save(any(ArticleTag.class));
  }

  @Test
  @DisplayName("성공: AI 태그 저장 - 10자 초과 시 자름")
  void saveAiTags_TrimLongTagName() {
    // given
    String longTagName = "매우긴태그이름입니다"; // 11자
    String trimmedTagName = longTagName.substring(0, 10); // 10자로 자름
    List<String> tagNames = List.of(longTagName);
    Article article = Article.builder().id(1L).title("제목").content("내용").build();
    Tag newTag = Tag.from(trimmedTagName);

    given(tagRepository.findByTagName(trimmedTagName)).willReturn(Optional.empty());
    given(tagRepository.save(any(Tag.class))).willReturn(newTag);
    given(articleTagRepository.save(any(ArticleTag.class))).willAnswer(inv -> inv.getArgument(0));

    // when
    tagWriter.saveAiTags(tagNames, article);

    // then
    then(tagRepository).should().findByTagName(trimmedTagName); // 10자로 잘린 이름으로 조회
    then(tagRepository).should().save(any(Tag.class));
    then(articleTagRepository).should().save(any(ArticleTag.class));
  }

  @Test
  @DisplayName("성공: 빈 태그 무시")
  void saveAiTags_IgnoreBlankTags() {
    // given
    List<String> tagNames = Arrays.asList("", "  ", "Valid");
    Article article = Article.builder().id(1L).title("제목").content("내용").build();
    Tag newTag = Tag.from("Valid");

    given(tagRepository.findByTagName("Valid")).willReturn(Optional.empty());
    given(tagRepository.save(any(Tag.class))).willReturn(newTag);
    given(articleTagRepository.save(any(ArticleTag.class))).willAnswer(inv -> inv.getArgument(0));

    // when
    tagWriter.saveAiTags(tagNames, article);

    // then
    then(articleTagRepository).should().deleteByArticle(article);
    then(tagRepository).should().findByTagName("Valid"); // Valid만 조회
    then(tagRepository).should(never()).findByTagName("");
    then(tagRepository).should(never()).findByTagName("  ");
    then(articleTagRepository).should(times(1)).save(any(ArticleTag.class)); // Valid만 저장
  }
}
