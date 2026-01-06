package com.mylog.comment.dto;

import com.mylog.comment.entity.Comment;
import java.time.LocalDateTime;

public record CommentResponse(
    Long id,
    String content,
    String author,
    Long memberId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
            comment.getId(), comment.getContent(),
            comment.getMember().getNickname(),
            comment.getMember().getId(),
            comment.getCreatedAt(), comment.getUpdatedAt()
        );
    }
}
