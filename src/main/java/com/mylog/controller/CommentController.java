package com.mylog.controller;

import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.comment.CommentCreateRequest;
import com.mylog.service.comment.CommentService;
import com.mylog.service.comment.CommentServiceFactory;
import com.mylog.service.comment.CommonCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentServiceFactory factory;
    private final CommonCommentService commentService;

    //댓글 생성
    @PostMapping
    public CommonResult createComment(
        @RequestBody @Valid CommentCreateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        CommentService service = factory.getCommentService(customUser.getProvider());
        service.createComment(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //게시글 댓글목록 조회

    //대댓글 목록 조회

    //댓글 수정

    //댓글 삭제

    //나의 댓글 조회

    //나의 게시글 댓글 조회

}
