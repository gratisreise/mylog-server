package com.mylog.controller;

import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.comment.CommentCreateRequest;
import com.mylog.model.dto.comment.CommentResponse;
import com.mylog.model.dto.comment.CommentUpdateRequest;
import com.mylog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api")
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/articles/{articleId}/comments")
    @Operation(summary = "댓글 생성")
    public CommonResult createComment(
        @RequestBody @Valid CommentCreateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        commentService.createComment(request, customUser);
        return ResponseService.getSuccessResult();
    }

    @GetMapping("/articles/{articleId}/comments")
    @Operation(summary = "댓글 목록 조회")
    public SingleResult<Page<CommentResponse>> getComments(
        @PathVariable Long articleId,
        @PageableDefault(sort="createdAt", direction = Direction.DESC)
        Pageable pageable
    ) {
        return ResponseService.getSingleResult(commentService.getComments(articleId, pageable));
    }

    @GetMapping("/articles/{articleId}/comments/{parentId}")
    @Operation(summary = "대댓글 목록 조회")
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

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "댓글 수정")
    public CommonResult updateComments(
        @RequestBody @Valid CommentUpdateRequest request,
        @PathVariable Long commentId,
        @AuthenticationPrincipal CustomUser customUser
    ) {
        commentService.updateComment(request, customUser, commentId);
        return ResponseService.getSuccessResult();
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    public CommonResult deleteComments(
        @PathVariable Long commentId,
        @AuthenticationPrincipal CustomUser customUser
    ) {
        commentService.deleteComment(commentId, customUser);
        return ResponseService.getSuccessResult();
    }

    @GetMapping("/comments/me")
    @Operation(summary = "내가 작성한 댓글 조회")
    public SingleResult<Page<CommentResponse>> getComments(
        @AuthenticationPrincipal CustomUser customUser,
        @PageableDefault(size = 20, sort="createdAt", direction = Direction.DESC)
        Pageable pageable
    ) {
        return ResponseService.getSingleResult(commentService.getMyComments(customUser, pageable));
    }

    @GetMapping("/articles/me/comments")
    @Operation(summary = "내게시글에 닥성된 댓글 조회")
    public SingleResult<Page<CommentResponse>> getMyArticlesComments(
        @AuthenticationPrincipal CustomUser customUser,
        @PageableDefault(size = 20, sort="createdAt", direction = Direction.DESC)
        Pageable pageable
    ){
        return ResponseService.getSingleResult(commentService.getComments(customUser, pageable));
    }

}
