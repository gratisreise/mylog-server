package com.mylog.controller;

import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.comment.CommentCreateRequest;
import com.mylog.dto.comment.CommentResponse;
import com.mylog.dto.comment.CommentUpdateRequest;
import com.mylog.service.comment.CommentService;
import com.mylog.service.comment.CommentServiceFactory;
import com.mylog.service.comment.CommonCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
        commentService.createComment(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //게시글 댓글목록 조회
    @GetMapping("/{articleId}")
    public SingleResult<Page<CommentResponse>> getComments(
        @PathVariable Long articleId,
        @PageableDefault(sort="createdAt", direction = Direction.DESC)
        Pageable pageable
    ) {
        return ResponseService.getSingleResult(commentService.getComments(articleId, pageable));
    }

    //대댓글 목록 조회
    @GetMapping("/{articleId}/{parentId}")
    public SingleResult<Page<CommentResponse>> getChildComments(
        @PathVariable Long articleId,
        @PathVariable Long parentId,
        @PageableDefault(size= 5, sort="createdAt", direction = Direction.DESC)
        Pageable pageable
    ) {
        return ResponseService.getSingleResult(
            commentService.getChildComments(articleId, parentId, pageable)
        );
    }

    //댓글 수정
    @PutMapping("/{commentId}")
    public CommonResult updateComments(
        @RequestBody @Valid CommentUpdateRequest request,
        @PathVariable Long commentId,
        @AuthenticationPrincipal CustomUser customUser
    ) {
        commentService.updateComment(request, commentId,customUser);
        return ResponseService.getSuccessResult();
    }

    //댓글 삭제
    @DeleteMapping("/{commentId}")
    public CommonResult deleteComments(
        @PathVariable Long commentId,
        @AuthenticationPrincipal CustomUser customUser
    ) {
        CommentService service = factory.getCommentService(customUser.getProvider());
        service.deleteComment(commentId, customUser);
        return ResponseService.getSuccessResult();
    }

    //나의 댓글 조회
    @GetMapping("/me")
    public SingleResult<Page<CommentResponse>> getComments(
        @AuthenticationPrincipal CustomUser customUser,
        @PageableDefault(size = 20, sort="createdAt", direction = Direction.DESC)
        Pageable pageable
    ) {
        CommentService service = factory.getCommentService(customUser.getProvider());
        return ResponseService.getSingleResult(service.getMyComments(customUser, pageable));
    }


    //나의 게시글 댓글 조회
    @GetMapping("/me/articles")
    public SingleResult<Page<CommentResponse>> getMyArticlesComments(
        @AuthenticationPrincipal CustomUser customUser,
        @PageableDefault(size = 20, sort="createdAt", direction = Direction.DESC)
        Pageable pageable
    ){
        CommentService service = factory.getCommentService(customUser.getProvider());
        return ResponseService.getSingleResult(service.getComments(customUser, pageable));
    }


}
