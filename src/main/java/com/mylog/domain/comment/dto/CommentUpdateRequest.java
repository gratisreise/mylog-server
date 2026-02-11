package com.mylog.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CommentUpdateRequest(
    @Length(min=2, max=200) @NotBlank String content
) { }
