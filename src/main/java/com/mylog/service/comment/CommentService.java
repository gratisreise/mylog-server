package com.mylog.service.comment;


import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.comment.CommentCreateRequest;
import com.mylog.dto.comment.CommentResponse;
import com.mylog.dto.comment.CommentUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    //댓글 생성
    void createComment(CommentCreateRequest request, CustomUser customUser);

    //댓글 수정
    void updateComment(CommentUpdateRequest request, Long commentId, CustomUser customUser);

    //댓글 삭제
    void deleteComment(Long commentId, CustomUser customUser);

    //나의 댓글 조회
    Page<CommentResponse> getMyComments(CustomUser customUser, Pageable pageable);

    //나의 게시글 댓글 조회
    Page<CommentResponse> getComments(CustomUser customUser, Pageable pageable);

}
