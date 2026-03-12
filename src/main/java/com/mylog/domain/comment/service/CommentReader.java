package com.mylog.domain.comment.service;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.article.service.ArticleReader;
import com.mylog.domain.comment.dto.CommentArticleResponse;
import com.mylog.domain.comment.dto.CommentResponse;
import com.mylog.domain.comment.dto.Reply;
import com.mylog.domain.comment.entity.Comment;
import com.mylog.domain.comment.repository.CommentRepository;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.MemberReader;
import jakarta.validation.constraints.Min;
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

  // 내가 작성한 댓글
  public Page<CommentResponse> getMyComments(Long memberId, Pageable pageable) {
    Member member = memberReader.getById(memberId);
    return commentRepository.findAllByMember(member, pageable).map(CommentResponse::from);
  }

  public Comment getById(Long commentId) {
    return commentRepository
        .findById(commentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
  }

  private CommentArticleResponse getCommentArticleResponse(Comment comment) {
    List<Reply> replies = getReplies(comment);
    return CommentArticleResponse.of(comment, replies);
  }

  private List<Reply> getReplies(Comment comment) {
    long parentId = comment.getId();
    List<Comment> comments = commentRepository.findByParent_Id(parentId);
    return comments.stream().map(Reply::from).toList();
  }

  public Page<CommentResponse> getComments(Long memberId, Pageable pageable) {
    return commentRepository.findByMemberId(memberId, pageable).map(CommentResponse::from);
  }

  public Page<CommentArticleResponse> getComments1(@Min(1) Long articleId, Pageable pageable) {
    Page<Comment> parentComments =
        commentRepository.findByArticle_IdAndParentIsNull(articleId, pageable);

    return parentComments.map(
        comment -> {
          List<Reply> replies = getReplies(comment);
          return CommentArticleResponse.of(comment, replies);
        });
  }

  public Page<Reply> getRepliesByParentId(Long parentId, Pageable pageable) {
    return commentRepository.findByParent_Id(parentId, pageable).map(Reply::from);
  }
}
