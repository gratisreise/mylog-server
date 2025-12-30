package com.mylog.api.comment.dto;

import com.mylog.comment.classes.Reply;
import com.mylog.comment.entity.Comment;
import java.time.LocalDateTime;
import java.util.List;

public record CommentArticleResponse(
    Long id,
    String content,
    String author,
    Long memberId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<Reply> replies
) {
    public static CommentArticleResponse of(Comment comment, List<Reply> replies){
        return new CommentArticleResponse(
            comment.getId(),
            comment.getContent(),
            comment.getMember().getNickname(),
            comment.getMember().getId(),
            comment.getCreatedAt(),
            comment.getUpdatedAt(),
            replies
        );
    }
}
