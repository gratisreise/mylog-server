package com.mylog.comment.service;


import com.mylog.article.entity.Article;
import com.mylog.article.service.ArticleReader;
import com.mylog.comment.repository.CommentRepository;
import com.mylog.comment.service.CommentReader;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
import com.mylog.notification.service.NotificationSettingWriter;
import com.mylog.notification.service.NotificationWriter;
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

//    public void createComment(Long articleId, CommentCreateRequest request, CustomUser customUser) {
//        Article article = articleReader.getById(articleId);
//        Member member = memberReader.getById(customUser.getMemberId());
//
//        Comment comment = request.toEntity(article, member);
//        commentRepository.save(comment);
//
//        //게시글 작성자에게 알림을 보냄
//        Member articleMember = article.getMember();
//        notificationSettingWriter.createNotificationSetting(articleMember, "comment");
//        notificationWriter.sendNotification(articleMember, article.getId(), "comment");
//    }
//
//    public void updateComment(CommentUpdateRequest request, CustomUser customUser, Long commentId) {
//        Comment comment = commentReader.getById(commentId);
//
//        if (!comment.isOwnedBy(customUser.getMemberId())) {
//            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
//        }
//
//        comment.update(request.content());
//    }
//
//    public void deleteComment(Long commentId, CustomUser customUser){
//        Comment comment = commentReader.getById(commentId);
//
//        if (comment.isOwnedBy(customUser.getMemberId())) {
//            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
//        }
//
//        commentRepository.deleteById(commentId);
//    }
}
