package com.mylog.service;

import com.mylog.classes.Reply;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.comment.CommentArticleResponse;
import com.mylog.model.dto.comment.CommentResponse;
import com.mylog.model.entity.Comment;
import com.mylog.model.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CommentRepository;
import com.mylog.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
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
    private final MemberReadService memberReadService;
    private final ArticleRepository articleRepository;

    public Page<CommentResponse> getMyComments(CustomUser customUser, Pageable pageable) {
        Member member = memberReadService.getById(customUser.getMemberId());
        return commentRepository.findAllByMember(member, pageable)
            .map(CommentResponse::new);
    }

    public Page<CommentResponse> getComments(CustomUser customUser, Pageable pageable) {
        Member member = memberReadService.getById(customUser.getMemberId());
        return commentRepository.findAllByArticle_Member(member, pageable)
            .map(CommentResponse::new);
    }

    public Page<CommentArticleResponse> getComments(Long articleId, Pageable pageable){
        if(!articleRepository.existsById(articleId)){
            throw new CMissingDataException("존재하지 않는 게시글 입니다.");
        }
        //
        return commentRepository.findByArticle_IdAndParentId(articleId, 0L, pageable)
            .map(comment -> new CommentArticleResponse(comment, getReplies(comment)));
    }

    private List<Reply> getReplies(Comment comment){
        long articleId = comment.getArticle().getId();
        long parentId = comment.getId();
        List<Comment> comments = commentRepository.findByArticle_IdAndParentId(articleId, parentId);
        List<Reply> replies = new ArrayList<>();
        for(Comment com : comments) replies.add(new Reply(com));
        return replies;
    }

    public Page<CommentResponse> getChildComments(Long articleId, Long parentId, Pageable pageable){
        if(!articleRepository.existsById(articleId)){
            throw new CMissingDataException("존재하지 않는 게시글입니다.");
        }
        if(!commentRepository.existsById(parentId)){
            throw new CMissingDataException("존재하지 않는 댓글입니다.");
        }
        return null;
//        return commentRepository.findByArticle_IdAndParentId(articleId, parentId, pageable)
//            .map(CommentResponse::new);
    }

    public Comment getById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(CMissingDataException::new);
    }
}
