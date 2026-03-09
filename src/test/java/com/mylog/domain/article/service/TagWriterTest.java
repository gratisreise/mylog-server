package com.mylog.domain.article.service;

import static org.mockito.BDDMockito.*;

import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.repository.TagRepository;
import java.util.List;
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
  @Mock private TagReader tagReader;

  @InjectMocks private TagWriter tagWriter;

  @Test
  @DisplayName("성공: 태그 저장 (현재 구현은 빈 메서드)")
  void saveTag_Success() {
    // given
    List<String> tags = List.of("Spring", "Java");
    Article article = Article.builder().id(1L).title("제목").content("내용").build();

    // when
    tagWriter.saveTag(tags, article);

    // then - 현재 구현이 비어있으므로 예외 없이 호출되면 성공
    // 추후 구현 시 검증 로직 추가 필요
  }
}
