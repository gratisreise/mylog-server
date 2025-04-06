package com.mylog.service.comment;

import com.mylog.annotations.ServiceType;
import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.comment.CommentCreateRequest;
import com.mylog.dto.comment.CommentResponse;
import com.mylog.dto.comment.CommentUpdateRequest;
import com.mylog.entity.Article;
import com.mylog.entity.Comment;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CommentRepository;
import com.mylog.repository.MemberRepository;
import com.mylog.service.notification.CommonNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@ServiceType(OauthProvider.SOCIAL)
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialCommentService implements CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final CommonNotificationService notificationService;

    @Override
    @Transactional
    public void createComment(CommentCreateRequest request, CustomUser customUser) {
        Article article = articleRepository.findById(request.getArticleId())
            .orElseThrow(CMissingDataException::new);

        Member member = memberRepository
            .findById(Long.valueOf(customUser.getUsername()))
            .orElseThrow(CMissingDataException::new);

        Comment comment =  Comment.builder()
            .article(article)
            .member(member)
            .content(request.getContent())
            .parentId(request.getParentCommentId())
            .build();

        commentRepository.save(comment);

        notificationService.sendNotification(
            article.getMember().getId(), comment.getId(), "comment");
    }

    @Override
    @Transactional
    public void updateComment(CommentUpdateRequest request, Long commentId, CustomUser customUser) {
        //댓글 객체불러오기
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(CMissingDataException::new);

        //맴버 검증
        if(!validateUpdate(customUser, comment)){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        };

        //수정
        comment.setContent(request.getContent());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, CustomUser customUser) {
        if(!validateDelete(commentId, customUser)){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        };

        commentRepository.deleteById(commentId);
    }

    @Override
    public Page<CommentResponse> getMyComments(CustomUser customUser, Pageable pageable) {
        Member member = memberRepository.findById(Long.valueOf(customUser.getUsername()))
            .orElseThrow(CMissingDataException::new);
        return commentRepository.findAllByMember(member, pageable)
            .map(CommentResponse::from);
    }

    @Override
    public Page<CommentResponse> getComments(CustomUser customUser, Pageable pageable) {
        Member member = memberRepository.findById(Long.valueOf(customUser.getUsername()))
            .orElseThrow(CMissingDataException::new);

        return commentRepository.findAllByArticle_Member(member, pageable)
            .map(CommentResponse::from);
    }

    private boolean validateDelete(Long commentId, CustomUser customUser) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(CMissingDataException::new);
        Article article = comment.getArticle();
        Long commentMemberId = comment.getMember().getId();
        Long articleMemberId = article.getMember().getId();
        Long userMemberId = Long.valueOf(customUser.getUsername());

        return userMemberId.equals(commentMemberId) || userMemberId.equals(articleMemberId);
    }

    private boolean validateUpdate(CustomUser customUser, Comment comment) {
        Long commentMemberId = comment.getMember().getId();
        Long userMemberId = memberRepository
            .findById(Long.valueOf(customUser.getUsername()))
            .orElseThrow(CMissingDataException::new)
            .getId();

        return commentMemberId.equals(userMemberId);
    }

}
