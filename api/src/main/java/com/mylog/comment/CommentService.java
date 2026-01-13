package com.mylog.comment;

import com.mylog.article.entity.Article;
import com.mylog.article.service.ArticleReader;
import com.mylog.auth.classes.CustomUser;
import com.mylog.comment.dto.CommentCreateRequest;
import com.mylog.comment.dto.CommentResponse;
import com.mylog.comment.dto.CommentUpdateRequest;
import com.mylog.comment.entity.Comment;
import com.mylog.comment.service.CommentReader;
import com.mylog.comment.service.CommentWriter;
import com.mylog.common.PageResponse;
import com.mylog.enums.ErrorMessage;
import com.mylog.exception.common.CMissingDataException;
import com.mylog.exception.common.CUnAuthorizedException;
import com.mylog.exception.common.CommonError;
import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
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
            throw new CMissingDataException(CommonError.ARTICLE_IS_EMPTY);
        }

        //저장할 댓글 생성
        Comment comment = generateComment(articleId, request, customUser);

        commentWriter.create(comment);
    }

    //게시글 댓글 조회
    public PageResponse<CommentResponse> getMyArticlesComments(Long articleId, Pageable pageable) {
        Page<CommentResponse> comments = commentReader.getComments(articleId, pageable)
            .map(CommentResponse::from);
        return PageResponse.from(comments);
    }

    //대댓글 조회
    public PageResponse<CommentResponse> getMyArticlesComments(Long articleId, Long parentId, Pageable pageable) {
        Page<CommentResponse> comments = commentReader.getComments(articleId, parentId, pageable)
            .map(CommentResponse::from);
        return PageResponse.from(comments);
    }

    //내가 작성한 댓글 조회
    public PageResponse<CommentResponse> getMyComments(CustomUser customUser, Pageable pageable) {
        Page<CommentResponse> comments = commentReader.getMyComments(customUser.getMemberId(), pageable)
            .map(CommentResponse::from);
        return PageResponse.from(comments);
    }

    //내 게시글의 댓글 조회
    public PageResponse<CommentResponse> getMyArticlesComments(CustomUser customUser, Pageable pageable) {
        Long memberId = customUser.getMemberId();
        Page<CommentResponse> comments = commentReader.getMyArticlesComments(memberId, pageable)
            .map(CommentResponse::from);
        return PageResponse.from(comments);
    }

    private Comment generateComment(Long articleId, CommentCreateRequest request,
        CustomUser customUser) {
        Article article = articleReader.getById(articleId);
        Member member = memberReader.getById(customUser.getMemberId());

        return request.toEntity(article, member);
    }

    @Transactional
    public void updateComment(CommentUpdateRequest request, CustomUser customUser,
        Long commentId) {
        Comment comment = commentReader.getById(commentId);
        Long memberId = customUser.getMemberId();
        if(!comment.isOwnedBy(memberId)){
            throw new CUnAuthorizedException(CommonError.NOT_YOUR_CATEGORY);
        }
        comment.update(request.content());
    }

    @Transactional
    public void deleteComment(Long commentId, CustomUser customUser) {
        Comment comment = commentReader.getById(commentId);
        Long memberId = customUser.getMemberId();
        if(!comment.isOwnedBy(memberId)){
            throw new CUnAuthorizedException(CommonError.NOT_YOUR_COMMENT);
        }
        commentWriter.deleteById(commentId);
    }
}
