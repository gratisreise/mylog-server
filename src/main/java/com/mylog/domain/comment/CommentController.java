<<<<<<<< HEAD:src/main/java/com/mylog/domain/comment/CommentController.java
<<<<<<<< HEAD:src/main/java/com/mylog/domain/comment/CommentController.java
package com.mylog.domain.comment;

import com.mylog.auth.classes.CustomUser;
import com.mylog.comment.dto.CommentCreateRequest;
import com.mylog.comment.dto.CommentResponse;
import com.mylog.comment.dto.CommentUpdateRequest;
import com.mylog.common.PageResponse;
import com.mylog.common.response.CommonResult;
import com.mylog.common.response.ResponseService;
import com.mylog.common.response.SingleResult;
import com.mylog.common.security.CustomUser;
import com.mylog.domain.comment.dto.CommentArticleResponse;
import com.mylog.domain.comment.dto.CommentCreateRequest;
import com.mylog.domain.comment.dto.CommentResponse;
import com.mylog.domain.comment.dto.CommentUpdateRequest;
import com.mylog.domain.comment.service.CommentReader;
import com.mylog.domain.comment.service.CommentWriter;
import com.mylog.response.CommonResult;
import com.mylog.response.ResponseService;
import com.mylog.response.SingleResult;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        @PathVariable Long articleId,
        @RequestBody @Valid CommentCreateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        commentService.createComment(articleId, request, customUser);
        return ResponseService.getSuccessResult();
    }

    @GetMapping("/articles/{articleId}/comments")
    @Operation(summary = "댓글 목록 조회")
    public SingleResult<PageResponse<CommentResponse>> getComments(
        @PathVariable Long articleId,
        @PageableDefault(sort="createdAt", direction = Direction.DESC)
        Pageable pageable
    ) {
        return ResponseService.getSingleResult(commentService.getMyArticlesComments(articleId, pageable));
    }

    @GetMapping("/articles/{articleId}/comments/{parentId}")
    @Operation(summary = "대댓글 조회")
    public SingleResult<PageResponse<CommentResponse>> getComments(
        @PathVariable Long articleId,
        @PathVariable Long parentId,
        @PageableDefault(sort="createdAt", direction = Direction.DESC)
        Pageable pageable
    ) {
        return ResponseService.getSingleResult(commentService.getMyArticlesComments(articleId, parentId, pageable));
    }

    @GetMapping("/comments/me")
    @Operation(summary = "내가 작성한 댓글 조회")
    public SingleResult<PageResponse<CommentResponse>> getComments(
        @AuthenticationPrincipal CustomUser customUser,
        @PageableDefault Pageable pageable
    ) {
        return ResponseService.getSingleResult(commentService.getMyComments(customUser, pageable));
    }

    @GetMapping("/articles/me/comments")
    @Operation(summary = "내 게시글에 작성된 댓글 조회")
    public SingleResult<PageResponse<CommentResponse>> getMyArticlesComments(
        @AuthenticationPrincipal CustomUser customUser,
        @PageableDefault Pageable pageable
    ){
        return ResponseService.getSingleResult(commentService.getMyArticlesComments(customUser, pageable));
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
}
