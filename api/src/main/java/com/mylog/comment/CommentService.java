package com.mylog.comment;

import com.mylog.article.entity.Article;
import com.mylog.article.service.ArticleReader;
import com.mylog.auth.CustomUser;
import com.mylog.comment.dto.CommentCreateRequest;
import com.mylog.comment.entity.Comment;
import com.mylog.comment.service.CommentReader;
import com.mylog.comment.service.CommentWriter;
import com.mylog.enums.ErrorMessage;
import com.mylog.exception.CMissingDataException;
import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {
    private final CommentReader commentReader;
    private final CommentWriter commentWriter;
    private final ArticleReader articleReader;
    private final MemberReader memberReader;


    @Transactional
    public void createComment(Long articleId, CommentCreateRequest request,
        CustomUser customUser) {

        // 게시글 존재 확인
        if(!articleReader.isExists(articleId)){
            throw new CMissingDataException(ErrorMessage.INVALID_ARTICLE);
        }

        //저장할 댓글 생성
        Comment comment = generateComment(articleId, request, customUser);

        commentWriter.create(comment);
    }

    private Comment generateComment(Long articleId, CommentCreateRequest request,
        CustomUser customUser) {
        Article article = articleReader.getById(articleId);
        Member member = memberReader.getById(customUser.getMemberId());

        return request.toEntity(article, member);
    }
}
