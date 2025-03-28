package com.mylog.dto.comment;

import lombok.Getter;

@Getter
public class CommentCreateRequest {
    private String content;
    private String articleAuthor;
    private String parentCommentId;
}
