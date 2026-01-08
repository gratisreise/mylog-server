package com.mylog.comment.dto;

import com.mylog.article.entity.Article;
import com.mylog.comment.entity.Comment;
import com.mylog.member.entity.Member;
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
