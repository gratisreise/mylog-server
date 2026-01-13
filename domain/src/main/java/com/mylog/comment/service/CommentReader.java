package com.mylog.comment.service;

import com.mylog.article.service.ArticleReader;
import com.mylog.comment.entity.Comment;
import com.mylog.comment.repository.CommentRepository;
import com.mylog.exception.common.CMissingDataException;
import com.mylog.exception.common.CommonError;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentReader {

    private final CommentRepository commentRepository;
    private final ArticleReader articleReader;

    //게시글 상세 댓글 목록조회
    public Page<Comment> getComments(Long articleId, Pageable pageable){
        if(!articleReader.isExists(articleId)){
            throw new CMissingDataException("존재하지 않는 게시글 입니다.");
        }

        return commentRepository.findByArticle_IdAndParentId(articleId, 0L, pageable);
    }

    //대댓글 목록조회
    public Page<Comment> getComments(Long articleId, Long parentId, Pageable pageable){
        if(!articleReader.isExists(articleId)){
            throw new CMissingDataException("존재하지 않는 게시글 입니다.");
        }

        return commentRepository.findByArticle_IdAndParentId(articleId, parentId, pageable);
    }

    //내가 작성한 댓글 조회
    public Page<Comment> getMyComments(Long memberId, Pageable pageable) {
        return commentRepository.findByMemberId(memberId, pageable);
    }

    //내 게시글의 댓글 조회
    public Page<Comment> getMyArticlesComments(Long memberId, Pageable pageable) {
        return commentRepository.findMyArticlesCommentsByMemberId(memberId, pageable);
    }

    public Comment getById(Long commentId) {
        return commentRepository.findById(commentId)
            .orElseThrow(() -> new CMissingDataException(CommonError.COMMENT_IS_EMPTY));
    }

}
