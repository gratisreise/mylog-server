package com.mylog.domain.comment.dto;

import com.mylog.domain.article.entity.Article;
import com.mylog.domain.comment.entity.Comment;
import com.mylog.domain.member.Member;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;


public record CommentCreateRequest (
    @Length(min=5, max=200) @NotBlank String content,
    long parentCommentId
){

    public Comment toEntity(Article article, Member member) {
        return Comment.builder()
            .article(article)
            .member(member)
            .content(content)
            .parentId(parentCommentId)
            .build();
    }
}
