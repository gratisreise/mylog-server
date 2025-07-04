package com.mylog.service;

import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.comment.CommentResponse;
import com.mylog.model.entity.Comment;
import com.mylog.model.entity.Member;
import com.mylog.exception.CMissingDataException;
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
@Transactional(readOnly = true)
public class CommentReadService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;

    public Page<CommentResponse> getMyComments(CustomUser customUser, Pageable pageable) {
        Member member = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
        return commentRepository.findAllByMember(member, pageable)
            .map(CommentResponse::from);
    }

    public Page<CommentResponse> getComments(CustomUser customUser, Pageable pageable) {
        Member member = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
        return commentRepository.findAllByArticle_Member(member, pageable)
            .map(CommentResponse::from);
    }


    public Page<CommentResponse> getComments(Long articleId, Pageable pageable){
        if(!articleRepository.existsById(articleId)){
            throw new CMissingDataException("존재하지 않는 게시글 입니다.");
        }
        return commentRepository.findByArticle_Id(articleId, pageable)
            .map(CommentResponse::from);
    }

    public Page<CommentResponse> getChildComments(Long articleId, Long parentId, Pageable pageable){
        if(!articleRepository.existsById(articleId)){
            throw new CMissingDataException("존재하지 않는 게시글입니다.");
        }
        if(!commentRepository.existsById(parentId)){
            throw new CMissingDataException("존재하지 않는 댓글입니다.");
        }
        return commentRepository.findByArticle_IdAndParentId(articleId, parentId, pageable)
            .map(CommentResponse::from);
    }

    public Comment getById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(CMissingDataException::new);
    }
}
