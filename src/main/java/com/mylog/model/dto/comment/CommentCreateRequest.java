package com.mylog.model.dto.comment;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;


public record CommentCreateRequest (
    @Length(min=2, max=200) @NotBlank String content,
    long parentCommentId
){ }
