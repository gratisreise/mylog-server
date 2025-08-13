package com.mylog.service.comment;

import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.comment.CommentCreateRequest;
import com.mylog.model.dto.comment.CommentUpdateRequest;
import com.mylog.model.entity.Article;
import com.mylog.model.entity.Comment;
import com.mylog.model.entity.Member;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.comment.CommentRepository;
import com.mylog.service.notification.NotificationService;
import com.mylog.service.article.ArticleReader;
import com.mylog.service.member.MemberReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentReader commentReader;
    private final MemberReader memberReader;
    private final ArticleReader articleReader;
    private final NotificationService notificationService;

    public void createComment(Long articleId, CommentCreateRequest request, CustomUser customUser) {
        Article article = articleReader.getArticleById(articleId);
        Member member = memberReader.getById(customUser.getMemberId());

        Comment comment = new Comment(article, member, request);
        commentRepository.save(comment);

        //알림 보내기 완전 비동기 처리가능
        notificationService.createNotificationSetting(article.getMember(), "comment");

        notificationService.sendNotification(article.getMember(), article.getId(), "comment");
    }

    public void updateComment(CommentUpdateRequest request, CustomUser customUser, Long commentId) {
        Comment comment = commentReader.getById(commentId);

        if (!validateUpdate(customUser, comment)) {
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }
        comment.setContent(request.content());
    }

    public void deleteComment(Long commentId, CustomUser customUser){
        if (!validateDelete(commentId, customUser)) {
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        commentRepository.deleteById(commentId);
    }

    private boolean validateDelete(Long commentId, CustomUser customUser) {
        Comment comment = commentReader.getById(commentId);
        Article article = comment.getArticle();

        long commentMemberId = comment.getMember().getId(); //댓글 작성자
        long articleMemberId = article.getMember().getId(); // 게시글 작성자

        long requestMemberId = memberReader.getById(customUser.getMemberId()).getId();

        return requestMemberId == commentMemberId || requestMemberId == articleMemberId;
    }

    private boolean validateUpdate(CustomUser customUser, Comment comment) {
        long commentMemberId = comment.getMember().getId();
        long requestMemberId = memberReader.getById(customUser.getMemberId()).getId();

        return commentMemberId == requestMemberId;
    }
}
