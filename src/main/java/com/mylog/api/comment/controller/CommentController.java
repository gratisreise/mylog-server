package com.mylog.api.comment.controller;

import com.mylog.api.comment.dto.CommentArticleResponse;
import com.mylog.api.comment.dto.CommentCreateRequest;
import com.mylog.api.comment.service.CommentReader;
import com.mylog.api.comment.dto.CommentResponse;
import com.mylog.api.comment.dto.CommentUpdateRequest;
import com.mylog.api.comment.service.CommentWriter;
import com.mylog.common.response.CommonResult;
import com.mylog.common.response.ResponseService;
import com.mylog.common.response.SingleResult;
import com.mylog.api.auth.CustomUser;
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
    private final CommentReader commentReader;
    private final CommentWriter commentWriter;

    @PostMapping("/articles/{articleId}/comments")
    @Operation(summary = "댓글 생성")
    public CommonResult createComment(
        @PathVariable Long articleId,
        @RequestBody @Valid CommentCreateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        commentWriter.createComment(articleId, request, customUser);
        return ResponseService.getSuccessResult();
    }

    @GetMapping("/articles/{articleId}/comments")
    @Operation(summary = "댓글 목록 조회")
    public SingleResult<Page<CommentArticleResponse>> getComments(
        @PathVariable Long articleId,
        @PageableDefault(sort="createdAt", direction = Direction.DESC)
        Pageable pageable
    ) {
        return ResponseService.getSingleResult(commentReader.getComments(articleId, pageable));
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "댓글 수정")
    public CommonResult updateComments(
        @RequestBody @Valid CommentUpdateRequest request,
        @PathVariable Long commentId,
        @AuthenticationPrincipal CustomUser customUser
    ) {
        commentWriter.updateComment(request, customUser, commentId);
        return ResponseService.getSuccessResult();
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    public CommonResult deleteComments(
        @PathVariable Long commentId,
        @AuthenticationPrincipal CustomUser customUser
    ) {
        commentWriter.deleteComment(commentId, customUser);
        return ResponseService.getSuccessResult();
    }

    @GetMapping("/comments/me")
    @Operation(summary = "내가 작성한 댓글 조회")
    public SingleResult<Page<CommentResponse>> getComments(
        @AuthenticationPrincipal CustomUser customUser,
        @PageableDefault Pageable pageable
    ) {
        return ResponseService.getSingleResult(commentReader.getMyComments(customUser, pageable));
    }

    @GetMapping("/articles/me/comments")
    @Operation(summary = "내게시글에 작성된 댓글 조회")
    public SingleResult<Page<CommentResponse>> getMyArticlesComments(
        @AuthenticationPrincipal CustomUser customUser,
        @PageableDefault Pageable pageable
    ){
        return ResponseService.getSingleResult(commentReader.getComments(customUser, pageable));
    }

}