package com.mylog.service;

import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.comment.CommentCreateRequest;
import com.mylog.dto.comment.CommentResponse;
import com.mylog.dto.comment.CommentUpdateRequest;
import com.mylog.entity.Article;
import com.mylog.entity.Comment;
import com.mylog.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CommentRepository;
import com.mylog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;
    private final NotificationService notificationService;

    //댓글 생성
    @Transactional
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

    //댓글 수정
    @Transactional
    public void updateComment(CommentUpdateRequest request, CustomUser customUser) {
        Comment comment = commentRepository.findById(request.getCommentId())
            .orElseThrow(CMissingDataException::new);

        if (!validateUpdate(customUser, comment)) {
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        comment.setContent(request.getContent());
    }

    //댓글 삭제
    public void deleteComment(Long commentId, CustomUser customUser){
        if (!validateDelete(commentId, customUser)) {
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        commentRepository.deleteById(commentId);
    }

    //나의 댓글 조회
    public Page<CommentResponse> getMyComments(CustomUser customUser, Pageable pageable) {
        Member member = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
        return commentRepository.findAllByMember(member, pageable)
            .map(CommentResponse::from);
    }

    //나의 게시글 댓글 조회
    public Page<CommentResponse> getComments(CustomUser customUser, Pageable pageable) {
        Member member = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
        return commentRepository.findAllByArticle_Member(member, pageable)
            .map(CommentResponse::from);
    }


    //게시글 댓글목록 조회
    public Page<CommentResponse> getComments(Long articleId, Pageable pageable){
        if(!articleRepository.existsById(articleId)){
            throw new CMissingDataException("존재하지 않는 게시글 입니다.");
        }
        return commentRepository.findByArticle_Id(articleId, pageable)
            .map(CommentResponse::from);
    };

    //대댓글 목록 조회
    public Page<CommentResponse> getChildComments(Long articleId, Long parentId, Pageable pageable){
        if(!articleRepository.existsById(articleId)){
            throw new CMissingDataException("존재하지 않는 게시글입니다.");
        }
        if(!commentRepository.existsById(parentId)){
            throw new CMissingDataException("존재하지 않는 댓글입니다.");
        }
        return commentRepository.findByArticle_IdAndParentId(articleId, parentId, pageable)
            .map(CommentResponse::from);
    };

    private boolean validateDelete(Long commentId, CustomUser customUser) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(CMissingDataException::new);
        Article article = comment.getArticle();

        Long commentMemberId = comment.getMember().getId(); //댓글 작성자
        Long articleMemberId = article.getMember().getId(); // 게시글 작성자

        //로직을 요청한 사람
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
