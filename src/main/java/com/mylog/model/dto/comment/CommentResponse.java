package com.mylog.model.dto.comment;

import com.mylog.model.entity.Comment;
import java.time.LocalDateTime;

public record CommentResponse(
    Long id,
    String content,
    String author,
    Long memberId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
    public  CommentResponse (Comment comment) {
        this(comment.getId(), comment.getContent(),
            comment.getMember().getNickname(),
            comment.getMember().getId(),
            comment.getCreatedAt(), comment.getUpdatedAt());
    }
}
