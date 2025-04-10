package com.mylog.service.comment;

import com.mylog.dto.comment.CommentResponse;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CommentRepository;
import com.mylog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommonCommentService {

    private final CommentRepository commentRepository;

    //게시글 댓글목록 조회
    public Page<CommentResponse> getComments(Long articleId, Pageable pageable){
        return commentRepository.findByArticleId(articleId, pageable)
            .map(CommentResponse::from);
    };

    //대댓글 목록 조회
    public Page<CommentResponse> getChildComments(Long articleId, Long parentId, Pageable pageable){
        return commentRepository.findByArticleIdAndParentId(articleId, parentId, pageable)
            .map(CommentResponse::from);
    };

}
