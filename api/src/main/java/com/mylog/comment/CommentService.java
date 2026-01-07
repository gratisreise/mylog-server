package com.mylog.comment;

import com.mylog.article.entity.Article;
import com.mylog.article.service.ArticleReader;
import com.mylog.auth.CustomUser;
import com.mylog.comment.dto.CommentArticleResponse;
import com.mylog.comment.dto.CommentCreateRequest;
import com.mylog.comment.dto.CommentResponse;
import com.mylog.comment.entity.Comment;
import com.mylog.comment.service.CommentReader;
import com.mylog.comment.service.CommentWriter;
import com.mylog.common.PageResponse;
import com.mylog.enums.ErrorMessage;
import com.mylog.exception.CMissingDataException;
import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    //게시글 댓글 조회
    public Page<CommentResponse> getComments(Long articleId, Pageable pageable) {
        Page<CommentResponse> comments = commentReader.getComments(articleId, pageable)
            .map(CommentResponse::from);
        return PageResponse.from(comments);
    }

    public Object getComments(Long articleId, Long commentId, Pageable pageable) {
        return null;
    }
}
