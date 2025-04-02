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

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ServiceType(OauthProvider.LOCAL)
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalCommentService implements CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;

    @Override
    @Transactional
    public void createComment(CommentCreateRequest request, CustomUser customUser) {
        //게시글 아이디로 게시글 작성자 찾기
        Article article = articleRepository.findById(request.getArticleId())
            .orElseThrow(CMissingDataException::new);

        //유저객체로 댓글 작성자 찾기
        Member member = memberRepository.findByEmail(customUser.getUsername())
            .orElseThrow(CMissingDataException::new);

        //커맨트 객체 생성
        Comment comment =  Comment.builder()
            .article(article)
            .member(member)
            .content(request.getContent())
            .parentId(request.getParentCommentId())
            .build();

        //저장
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void updateComment(CommentUpdateRequest request, Long commentId, CustomUser customUser) {
        //댓글 객체불러오기
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(CMissingDataException::new);

        //맴버 검증 validateUpdate
        Long commentMemberId = comment.getMember().getId();
        Long requestMemberId = memberRepository.findByEmail(customUser.getUsername())
            .orElseThrow(CMissingDataException::new)
            .getId();

        if(commentMemberId == null || requestMemberId == null){
            throw new CMissingDataException("존재하지 않는 유저입니다.");
        }
        if(!commentMemberId.equals(requestMemberId)){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        //수정
        comment.setContent(request.getContent());
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, CustomUser customUser) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(CMissingDataException::new);
        Article article = comment.getArticle();

        //검증 validateDelete
        Long commentMemberId = comment.getMember().getId();
        Long articleMemberId = article.getMember().getId();
        Long userMemberId = memberRepository.findByEmail(customUser.getUsername())
            .orElseThrow(CMissingDataException::new)
            .getId();

        if(commentMemberId == null || articleMemberId == null || userMemberId == null){
            throw new CMissingDataException("존재 하지 않는 유저입니다.");
        }

        if(!userMemberId.equals(commentMemberId) && !userMemberId.equals(articleMemberId)){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        commentRepository.deleteById(commentId);
    }

    @Override
    public Page<CommentResponse> getMyComments(CustomUser customUser, Pageable pageable) {
        Member member = memberRepository.findByEmail(customUser.getUsername())
            .orElseThrow(CMissingDataException::new);
        return commentRepository.findAllByMember(member, pageable)
            .map(CommentResponse::from);
    }

    @Override
    public Page<CommentResponse> getComments(CustomUser customUser, Pageable pageable) {
        Member member = memberRepository.findByEmail(customUser.getUsername())
            .orElseThrow(CMissingDataException::new);
        return commentRepository.findAllByArticle_Member(member, pageable)
            .map(CommentResponse::from);
    }
}
