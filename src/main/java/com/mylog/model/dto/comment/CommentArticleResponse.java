package com.mylog.model.dto.comment;

import com.mylog.classes.Reply;
import com.mylog.model.entity.Comment;
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
    public CommentArticleResponse(Comment comment, List<Reply> replies){
        this(comment.getId(), comment.getContent(),
            comment.getMember().getNickname(),
            comment.getMember().getId(),
            comment.getCreatedAt(), comment.getUpdatedAt(),
            replies
        );
    }
}
