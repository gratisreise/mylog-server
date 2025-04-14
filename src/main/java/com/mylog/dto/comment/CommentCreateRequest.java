package com.mylog.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateRequest {
    @Length(min=2, max=200)
    private String content;
    private Long articleId;
    private long parentCommentId;
}
