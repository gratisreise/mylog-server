package com.mylog.domain.comment.dto;

import com.mylog.domain.comment.entity.Comment;
import com.mylog.domain.member.entity.Member;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record Reply(
    Long id,
    String content,
    String author,
    Long memberId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
  public static Reply from(Comment comment) {
    Member member = comment.getMember();
    return Reply.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .author(member.getNickname())
        .memberId(member.getId())
        .createdAt(comment.getCreatedAt())
        .updatedAt(comment.getUpdatedAt())
        .build();
  }
}
