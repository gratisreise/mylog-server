package com.mylog.api.comment;

import com.mylog.exception.CUnAuthorizedException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.domain.entity.Article;
import com.mylog.domain.entity.Comment;
import com.mylog.domain.entity.Member;
import com.mylog.api.article.ArticleReader;
import com.mylog.api.member.MemberReader;
import com.mylog.service.notification.NotificationService;
import com.mylog.service.notificationsetting.NotificationSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentWriter {

    private final CommentRepository commentRepository;
    private final CommentReader commentReader;
    private final MemberReader memberReader;
    private final ArticleReader articleReader;
    private final NotificationService notificationService;
    private final NotificationSettingService notificationSettingService;

    public void createComment(Long articleId, CommentCreateRequest request, CustomUser customUser) {
        Article article = articleReader.getArticleById(articleId);
        Member member = memberReader.getById(customUser.getMemberId());

        Comment comment = request.toEntity(article, member);
        commentRepository.save(comment);

        //게시글 작성자에게 알림을 보냄
        Member articleMember = article.getMember();
        notificationSettingService.createNotificationSetting(articleMember, "comment");
        notificationService.sendNotification(articleMember, article.getId(), "comment");
    }

    public void updateComment(CommentUpdateRequest request, CustomUser customUser, Long commentId) {
        Comment comment = commentReader.getById(commentId);

        if (!comment.isOwnedBy(customUser.getMemberId())) {
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        comment.update(request.content());
    }

    public void deleteComment(Long commentId, CustomUser customUser){
        Comment comment = commentReader.getById(commentId);

        if (comment.isOwnedBy(customUser.getMemberId())) {
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        commentRepository.deleteById(commentId);
    }
}
