package com.mylog.dto.comment;

import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
public class CommentCreateRequest {
    @Length(min=2, max=200)
    private String content;
    private Long articleId;
    private long parentCommentId;
}
