package com.mylog.service;

import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.comment.CommentCreateRequest;
import com.mylog.model.dto.comment.CommentUpdateRequest;
import com.mylog.model.entity.Article;
import com.mylog.model.entity.Comment;
import com.mylog.model.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CommentRepository;
import com.mylog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentWriteService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final NotificationService notificationService;

    public void createComment(CommentCreateRequest request, CustomUser customUser) {
        Article article = articleRepository.findById(request.getArticleId())
            .orElseThrow(CMissingDataException::new);

        Member member = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);

        Comment comment = Comment.builder()
            .article(article)
            .member(member)
            .content(request.getContent())
            .parentId(request.getParentCommentId())
            .build();

        commentRepository.save(comment);

        notificationService.createNotificationSetting(article.getMember(), "comment");

        notificationService.sendNotification(
            article.getMember(), article.getId(), "comment");
    }

    public void updateComment(CommentUpdateRequest request, CustomUser customUser, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(CMissingDataException::new);

        if (!validateUpdate(customUser, comment)) {
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        comment.setContent(request.getContent());
    }

    public void deleteComment(Long commentId, CustomUser customUser){
        if (!validateDelete(commentId, customUser)) {
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        commentRepository.deleteById(commentId);
    }

    private boolean validateDelete(Long commentId, CustomUser customUser) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(CMissingDataException::new);
        Article article = comment.getArticle();

        Long commentMemberId = comment.getMember().getId(); //댓글 작성자
        Long articleMemberId = article.getMember().getId(); // 게시글 작성자

        Long userMemberId = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new)
            .getId();

        return userMemberId.equals(commentMemberId) || userMemberId.equals(articleMemberId);
    }

    private boolean validateUpdate(CustomUser customUser, Comment comment) {
        Long commentMemberId = comment.getMember().getId();
        Long requestMemberId = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new)
            .getId();

        return commentMemberId.equals(requestMemberId);
    }
}
