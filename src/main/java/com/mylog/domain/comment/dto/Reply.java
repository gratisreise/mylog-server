package com.mylog.domain.comment.dto;

import com.mylog.domain.comment.entity.Comment;
import com.mylog.domain.member.entity.Member;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reply {
  private Long id;
  private String content;
  private String author;
  private Long memberId;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

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
