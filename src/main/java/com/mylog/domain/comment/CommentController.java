package com.mylog.domain.comment;

import com.mylog.common.annotations.AuthenticatedMember;
import com.mylog.common.response.PageResponse;
import com.mylog.common.response.SuccessResponse;
import com.mylog.domain.comment.dto.CommentArticleResponse;
import com.mylog.domain.comment.dto.CommentCreateRequest;
import com.mylog.domain.comment.dto.CommentResponse;
import com.mylog.domain.comment.dto.CommentUpdateRequest;
import com.mylog.domain.comment.dto.Reply;
import com.mylog.domain.comment.service.CommentReader;
import com.mylog.domain.comment.service.CommentWriter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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

  @Operation(summary = "댓글 생성")
  @PostMapping("/articles/{articleId}/comments")
  public ResponseEntity<SuccessResponse<Long>> createComment(
      @PathVariable Long articleId,
      @RequestBody @Valid CommentCreateRequest request,
      @AuthenticatedMember Long memberId) {
    Long commentId = commentWriter.createComment(articleId, request, memberId);
    return SuccessResponse.toCreated(commentId);
  }

  @Operation(summary = "댓글 목록 조회")
  @GetMapping("/articles/{articleId}/comments")
  public ResponseEntity<SuccessResponse<PageResponse<CommentArticleResponse>>> getComments(
      @PathVariable Long articleId,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
    Page<CommentArticleResponse> comments = commentReader.getComments1(articleId, pageable);
    return SuccessResponse.toOk(PageResponse.from(comments));
  }

  @Operation(summary = "대댓글 목록 조회")
  @GetMapping("/comments/{parentId}/replies")
  public ResponseEntity<SuccessResponse<PageResponse<Reply>>> getReplies(
      @PathVariable Long parentId,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
    Page<Reply> replies = commentReader.getRepliesByParentId(parentId, pageable);
    return SuccessResponse.toOk(PageResponse.from(replies));
  }

  @Operation(summary = "내가 작성한 댓글 조회")
  @GetMapping("/comments/me")
  public ResponseEntity<SuccessResponse<PageResponse<CommentResponse>>> getMyComments(
      @AuthenticatedMember Long memberId,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
    Page<CommentResponse> comments = commentReader.getMyComments(memberId, pageable);
    return SuccessResponse.toOk(PageResponse.from(comments));
  }

  @Operation(summary = "내 게시글에 작성된 댓글 조회")
  @GetMapping("/comments/me/received")
  public ResponseEntity<SuccessResponse<PageResponse<CommentResponse>>> getReceivedComments(
      @AuthenticatedMember Long memberId,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
    Page<CommentResponse> comments = commentReader.getComments(memberId, pageable);
    return SuccessResponse.toOk(PageResponse.from(comments));
  }

  @Operation(summary = "댓글 수정")
  @PatchMapping("/comments/{commentId}")
  public ResponseEntity<SuccessResponse<Void>> updateComment(
      @RequestBody @Valid CommentUpdateRequest request,
      @PathVariable Long commentId,
      @AuthenticatedMember Long memberId) {
    commentWriter.updateComment(request, memberId, commentId);
    return SuccessResponse.toNoContent();
  }

  @Operation(summary = "댓글 삭제")
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<SuccessResponse<Void>> deleteComment(
      @PathVariable Long commentId, @AuthenticatedMember Long memberId) {
    commentWriter.deleteComment(commentId, memberId);
    return SuccessResponse.toNoContent();
  }
}
