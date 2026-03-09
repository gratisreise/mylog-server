package com.mylog.domain.comment.dto;

import com.mylog.domain.comment.entity.Comment;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record CommentArticleResponse(
    Long id,
    String content,
    String author,
    Long memberId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<Reply> replies) {

  public static CommentArticleResponse of(Comment comment, List<Reply> replies) {
    return CommentArticleResponse.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .author(comment.getMember().getNickname())
        .memberId(comment.getMember().getId())
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .replies(replies)
        .build();
  }
}
