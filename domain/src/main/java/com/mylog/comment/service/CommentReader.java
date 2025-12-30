package com.mylog.api.comment.service;

import com.mylog.article.service.ArticleReader;
import com.mylog.api.comment.dto.CommentArticleResponse;
import com.mylog.api.comment.dto.CommentResponse;
import com.mylog.comment.classes.Reply;
import com.mylog.comment.entity.Comment;
import com.mylog.api.comment.repository.CommentRepository;
import com.mylog.common.exception.CMissingDataException;
import com.mylog.api.auth.CustomUser;
import com.mylog.api.member.entity.Member;
import com.mylog.api.member.service.MemberReader;
import java.util.List;
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
    private final MemberReader memberReader;
    private final ArticleReader articleReader;

    //내가 작성한 댓글
    public Page<CommentResponse> getMyComments(CustomUser customUser, Pageable pageable) {
        Member member = memberReader.getById(customUser.getMemberId());
        return commentRepository.findAllByMember(member, pageable)
            .map(CommentResponse::from);
    }

    //내 게시글의 댓글
    public Page<CommentResponse> getComments(CustomUser customUser, Pageable pageable) {
        Member member = memberReader.getById(customUser.getMemberId());
        return commentRepository.findMyArticlesComments(member, pageable)
            .map(CommentResponse::from);
    }

    //게시글 상세 댓글 목록조회
    public Page<CommentArticleResponse> getComments(Long articleId, Pageable pageable){
        if(!articleReader.isExists(articleId)){
            throw new CMissingDataException("존재하지 않는 게시글 입니다.");
        }

        return commentRepository.findByArticle_IdAndParentId(articleId, 0L, pageable)
            .map(this::getCommentArticleResponse);
    }

    public Comment getById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(CMissingDataException::new);
    }

    private CommentArticleResponse getCommentArticleResponse(Comment comment){
        List<Reply> replies = getReplies(comment);
        return CommentArticleResponse.of(comment, replies);
    }


    private List<Reply> getReplies(Comment comment){
        long articleId = comment.getArticle().getId();
        long parentId = comment.getId();
        List<Comment> comments = commentRepository.findByArticle_IdAndParentId(articleId, parentId);
        return comments.stream().map(Reply::new).toList();
    }

}
