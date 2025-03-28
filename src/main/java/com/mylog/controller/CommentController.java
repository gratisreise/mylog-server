package com.mylog.controller;

import com.mylog.service.comment.CommentServiceFactory;
import com.mylog.service.comment.CommonCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentServiceFactory factory;
    private final CommonCommentService commentService;

    //댓글 생성

    //게시글 댓글목록 조회

    //대댓글 목록 조회

    //댓글 수정

    //댓글 삭제

    //나의 댓글 조회

    //나의 게시글 댓글 조회

}
