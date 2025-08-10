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
import com.mylog.service.article.ArticleReadService;
import com.mylog.service.member.MemberReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentWriteService {

    private final CommentRepository commentRepository;
    private final CommentReadService commentReadService;
    private final MemberReadService memberReadService;
    private final ArticleReadService articleReadService;
    private final NotificationService notificationService;

    public void createComment(Long articleId, CommentCreateRequest request, CustomUser customUser) {
        Article article = articleReadService.getArticleById(articleId);
        Member member = memberReadService.getById(customUser.getMemberId());

        Comment comment = new Comment(article, member, request);
        commentRepository.save(comment);

        //알림 보내기 완전 비동기 처리가능
        notificationService.createNotificationSetting(article.getMember(), "comment");

        notificationService.sendNotification(article.getMember(), article.getId(), "comment");
    }

    public void updateComment(CommentUpdateRequest request, CustomUser customUser, Long commentId) {
        Comment comment = commentReadService.getById(commentId);

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
        Comment comment = commentReadService.getById(commentId);
        Article article = comment.getArticle();

        long commentMemberId = comment.getMember().getId(); //댓글 작성자
        long articleMemberId = article.getMember().getId(); // 게시글 작성자

        long requestMemberId = memberReadService.getById(customUser.getMemberId()).getId();

        return requestMemberId == commentMemberId || requestMemberId == articleMemberId;
    }

    private boolean validateUpdate(CustomUser customUser, Comment comment) {
        long commentMemberId = comment.getMember().getId();
        long requestMemberId = memberReadService.getById(customUser.getMemberId()).getId();

        return commentMemberId == requestMemberId;
    }
}
