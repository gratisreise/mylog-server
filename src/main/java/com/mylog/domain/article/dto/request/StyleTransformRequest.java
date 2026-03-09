package com.mylog.domain.article.dto.request;

import com.mylog.common.enums.WritingStyle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record StyleTransformRequest(
    @NotBlank @Length(min = 10, max = 3000) String content, @NotNull WritingStyle writingStyle) {}
