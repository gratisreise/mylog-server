package com.mylog.domain.article.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.article.entity.Tag;
import com.mylog.domain.article.repository.TagRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("TagReader 단위 테스트")
class TagReaderTest {

  @Mock private TagRepository tagRepository;

  @InjectMocks private TagReader tagReader;

  private static final String TAG_NAME = "Spring";
  private static final Long TAG_ID = 1L;

  private Tag createTag() {
    return Tag.builder().id(TAG_ID).tagName(TAG_NAME).build();
  }

  @Nested
  @DisplayName("태그명으로 조회")
  class GetTagByTagName {

    @Test
    @DisplayName("성공: 태그명으로 태그 조회")
    void getTagByTagName_Success() {
      // given
      Tag expectedTag = createTag();
      given(tagRepository.findByTagName(TAG_NAME)).willReturn(Optional.of(expectedTag));

      // when
      Tag result = tagReader.getTagByTagName(TAG_NAME);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getTagName()).isEqualTo(TAG_NAME);
      then(tagRepository).should().findByTagName(TAG_NAME);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 태그 조회 시 예외 발생")
    void getTagByTagName_NotFound_ThrowsException() {
      // given
      given(tagRepository.findByTagName(TAG_NAME)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> tagReader.getTagByTagName(TAG_NAME))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.TAG_NOT_FOUND);

      then(tagRepository).should().findByTagName(TAG_NAME);
    }
  }
}
