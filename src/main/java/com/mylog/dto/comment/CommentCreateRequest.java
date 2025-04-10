package com.mylog.dto.comment;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class CommentCreateRequest {
    @Length(min=2, max=200)
    private String content;
    private Long articleId;
    private long parentCommentId;
}
