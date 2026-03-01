package com.mylog.domain.comment.service;


import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.ArticleReader;
import com.mylog.domain.comment.dto.CommentCreateRequest;
import com.mylog.domain.comment.dto.CommentUpdateRequest;
import com.mylog.domain.comment.entity.Comment;
import com.mylog.domain.comment.repository.CommentRepository;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.service.MemberReader;
import com.mylog.domain.notification.service.NotificationWriter;
import com.mylog.domain.notificationsetting.service.NotificationSettingWriter;
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
    private final NotificationWriter notificationWriter;
    private final NotificationSettingWriter notificationSettingWriter;

    public Long createComment(Long articleId, CommentCreateRequest request, Long memberId) {
        Article article = articleReader.getArticleById(articleId);
        Member member = memberReader.getById(memberId);

        Comment comment = request.toEntity(article, member);
        commentRepository.save(comment);

        //게시글 작성자에게 알림을 보냄
        Member articleMember = article.getMember();
        notificationSettingWriter.createNotificationSetting(articleMember, "comment");
        notificationWriter.sendNotification(articleMember, article.getId(), "comment");

        return comment.getId();
    }

    public void updateComment(CommentUpdateRequest request, Long memberId, Long commentId) {
        Comment comment = commentReader.getById(commentId);

        if (!comment.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.COMMENT_FORBIDDEN);
        }

        comment.update(request.content());
    }

    public void deleteComment(Long commentId, Long memberId) {
        Comment comment = commentReader.getById(commentId);

        if (!comment.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.COMMENT_FORBIDDEN);
        }

        commentRepository.deleteById(commentId);
    }
}
