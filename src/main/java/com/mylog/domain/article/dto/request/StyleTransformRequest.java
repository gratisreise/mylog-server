package com.mylog.domain.article.dto.request;

import com.mylog.common.enums.WritingStyle;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record StyleTransformRequest(
    @NotBlank @Length(min = 10, max = 3000) String content,
    WritingStyle writingStyle,
    Long customStyleId) {

  @AssertTrue(message = "공통 스타일 또는 커스텀 스타일 ID 중 하나는 필수입니다.")
  public boolean isValidStyle() {
    return writingStyle != null || customStyleId != null;
  }
}
