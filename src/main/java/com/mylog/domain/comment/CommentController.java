package com.mylog.domain.comment;

import com.mylog.common.annotations.MemberId;
import com.mylog.common.response.PageResponse;
import com.mylog.common.response.SuccessResponse;
import com.mylog.domain.comment.dto.CommentArticleResponse;
import com.mylog.domain.comment.dto.CommentCreateRequest;
import com.mylog.domain.comment.dto.CommentResponse;
import com.mylog.domain.comment.dto.CommentUpdateRequest;
import com.mylog.domain.comment.service.CommentReader;
import com.mylog.domain.comment.service.CommentWriter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
    public ResponseEntity<SuccessResponse<Long>> createComment(
        @PathVariable @Min(1) Long articleId,
        @RequestBody @Valid CommentCreateRequest request,
        @MemberId Long memberId
    ) {
        Long commentId = commentWriter.createComment(articleId, request, memberId);
        return SuccessResponse.toCreated(commentId);
    }

    @GetMapping("/articles/{articleId}/comments")
    @Operation(summary = "댓글 목록 조회")
    public ResponseEntity<SuccessResponse<PageResponse<CommentArticleResponse>>> getComments(
        @PathVariable @Min(1) Long articleId,
        @PageableDefault(sort = "createdAt", direction = Direction.DESC)
        Pageable pageable
    ) {
        Page<CommentArticleResponse> comments = commentReader.getComments(articleId, pageable);
        return SuccessResponse.toOk(PageResponse.from(comments));
    }

    @GetMapping("/comments/me")
    @Operation(summary = "내가 작성한 댓글 조회")
    public ResponseEntity<SuccessResponse<PageResponse<CommentResponse>>> getMyComments(
        @MemberId Long memberId,
        @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
    ) {
        Page<CommentResponse> comments = commentReader.getMyComments(memberId, pageable);
        return SuccessResponse.toOk(PageResponse.from(comments));
    }

    @GetMapping("/comments/me/received")
    @Operation(summary = "내 게시글에 작성된 댓글 조회")
    public ResponseEntity<SuccessResponse<PageResponse<CommentResponse>>> getReceivedComments(
        @MemberId Long memberId,
        @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable
    ) {
        Page<CommentResponse> comments = commentReader.getComments(memberId, pageable);
        return SuccessResponse.toOk(PageResponse.from(comments));
    }

    @PatchMapping("/comments/{commentId}")
    @Operation(summary = "댓글 수정")
    public ResponseEntity<SuccessResponse<Void>> updateComment(
        @RequestBody @Valid CommentUpdateRequest request,
        @PathVariable @Min(1) Long commentId,
        @MemberId Long memberId
    ) {
        commentWriter.updateComment(request, memberId, commentId);
        return SuccessResponse.toOk(null);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 삭제")
    public ResponseEntity<SuccessResponse<Void>> deleteComment(
        @PathVariable @Min(1) Long commentId,
        @MemberId Long memberId
    ) {
        commentWriter.deleteComment(commentId, memberId);
        return SuccessResponse.toNoContent(null);
    }
}
