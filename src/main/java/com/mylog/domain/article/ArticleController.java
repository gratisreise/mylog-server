package com.mylog.domain.article;

import com.mylog.common.annotations.MemberId;
import com.mylog.common.response.PageResponse;
import com.mylog.common.response.SuccessResponse;
import com.mylog.domain.article.dto.request.ArticleCreateRequest;
import com.mylog.domain.article.dto.request.ArticleUpdateRequest;
import com.mylog.domain.article.dto.request.StyleTransformRequest;
import com.mylog.domain.article.dto.response.ArticleCreateResponse;
import com.mylog.domain.article.dto.response.ArticleResponse;
import com.mylog.domain.article.dto.response.ArticleSummaryResponse;
import com.mylog.domain.article.dto.response.StyleTransformResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

  private final ArticleService articleService;

  // === 생성/수정/삭제 ===

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "게시글 생성")
  public ResponseEntity<SuccessResponse<ArticleCreateResponse>> createArticle(
      @RequestPart(value = "file") MultipartFile file,
      @RequestPart(value = "request") @Valid ArticleCreateRequest request,
      @MemberId Long memberId) {
    return SuccessResponse.toOk(articleService.createArticle(request, memberId, file));
  }

  @PutMapping(value = "/{articleId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @Operation(summary = "게시글 수정")
  public ResponseEntity<SuccessResponse<Void>> updateArticle(
      @RequestPart(value = "request") @Valid ArticleUpdateRequest request,
      @RequestPart(required = false, value = "file") MultipartFile file,
      @MemberId Long memberId,
      @PathVariable Long articleId) {
    articleService.updateArticle(request, memberId, file, articleId);
    return SuccessResponse.toNoContent();
  }

  @DeleteMapping("/{articleId}")
  @Operation(summary = "게시글 삭제")
  public ResponseEntity<SuccessResponse<Void>> deleteArticle(
      @MemberId Long memberId, @PathVariable Long articleId) {
    articleService.deleteArticle(articleId, memberId);
    return SuccessResponse.toNoContent();
  }

  // === 조회 ===

  @GetMapping("/{articleId}")
  @Operation(summary = "게시글 상세")
  public ResponseEntity<SuccessResponse<ArticleResponse>> getArticle(@PathVariable Long articleId) {
    return SuccessResponse.toOk(articleService.getArticle(articleId));
  }

  @GetMapping
  @Operation(summary = "전체 게시글 목록 조회")
  public ResponseEntity<SuccessResponse<PageResponse<ArticleResponse>>> getArticles(
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
    return SuccessResponse.toOk(articleService.getArticles(pageable));
  }

  @GetMapping("/search")
  @Operation(summary = "전체 게시글 검색")
  public ResponseEntity<SuccessResponse<PageResponse<ArticleResponse>>> searchArticles(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String tag,
      @RequestParam(required = false) Long categoryId,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
    return SuccessResponse.toOk(articleService.searchArticles(keyword, tag, categoryId, pageable));
  }

  @GetMapping("/me")
  @Operation(summary = "내 게시글 목록 조회")
  public ResponseEntity<SuccessResponse<PageResponse<ArticleResponse>>> getMyArticles(
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable,
      @MemberId Long memberId) {
    return SuccessResponse.toOk(articleService.getArticles(pageable, memberId));
  }

  @GetMapping("/me/search")
  @Operation(summary = "내 게시글 검색")
  public ResponseEntity<SuccessResponse<PageResponse<ArticleResponse>>> searchMyArticles(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String tag,
      @RequestParam(required = false) Long categoryId,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable,
      @MemberId Long memberId) {
    return SuccessResponse.toOk(
        articleService.searchMyArticles(keyword, tag, categoryId, pageable, memberId));
  }

  // === AI 서비스 ===

  @PostMapping("/transform-style")
  @Operation(summary = "AI 문체 변환")
  public ResponseEntity<SuccessResponse<StyleTransformResponse>> transformWritingStyle(
      @RequestBody @Valid StyleTransformRequest request) {
    return SuccessResponse.toOk(articleService.transformWritingStyle(request));
  }

  @GetMapping("/{articleId}/summary")
  @Operation(summary = "AI 요약 조회")
  public ResponseEntity<SuccessResponse<ArticleSummaryResponse>> getArticleSummary(
      @PathVariable Long articleId) {
    return SuccessResponse.toOk(articleService.getArticleSummary(articleId));
  }
}
