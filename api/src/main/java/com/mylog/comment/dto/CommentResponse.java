package com.mylog.comment.dto;

import com.mylog.comment.entity.Comment;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record CommentResponse(
    Long id,
    String content,
    String author,
    Long memberId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
            .id(comment.getId())
            .content(comment.getContent())
            .author(comment.getContent())
            .memberId(comment.getMember().getId())
            .createdAt(comment.getCreatedAt())
            .updatedAt(comment.getUpdatedAt())
            .build();
    }
}
