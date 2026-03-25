package com.mylog.domain.article;

import com.mylog.common.annotations.AuthenticatedMember;
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

  // === 쓰기 ===

  @Operation(summary = "게시글 생성")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<SuccessResponse<ArticleCreateResponse>> createArticle(
      @RequestPart(value = "file") MultipartFile file,
      @RequestPart(value = "request") @Valid ArticleCreateRequest request,
      @AuthenticatedMember Long memberId) {
    return SuccessResponse.toCreated(articleService.createArticle(request, memberId, file));
  }

  @Operation(summary = "게시글 수정")
  @PutMapping(value = "/{articleId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<SuccessResponse<Void>> updateArticle(
      @RequestPart(value = "request") @Valid ArticleUpdateRequest request,
      @RequestPart(required = false, value = "file") MultipartFile file,
      @AuthenticatedMember Long memberId,
      @PathVariable Long articleId) {
    articleService.updateArticle(request, memberId, file, articleId);
    return SuccessResponse.toNoContent();
  }

  @Operation(summary = "게시글 삭제")
  @DeleteMapping("/{articleId}")
  public ResponseEntity<SuccessResponse<Void>> deleteArticle(
      @AuthenticatedMember Long memberId, @PathVariable Long articleId) {
    articleService.deleteArticle(articleId, memberId);
    return SuccessResponse.toNoContent();
  }

  // === 읽기 ===

  @Operation(summary = "게시글 상세")
  @GetMapping("/{articleId}")
  public ResponseEntity<SuccessResponse<ArticleResponse>> getArticle(@PathVariable Long articleId) {
    return SuccessResponse.toOk(articleService.getArticle(articleId));
  }

  @Operation(summary = "전체 게시글 목록/검색 조회")
  @GetMapping
  public ResponseEntity<SuccessResponse<PageResponse<ArticleResponse>>> getArticles(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String tag,
      @RequestParam(required = false) Long categoryId,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
    return SuccessResponse.toOk(
        articleService.getArticles(pageable, null, keyword, tag, categoryId));
  }

  @Operation(summary = "내 게시글 목록/검색 조회")
  @GetMapping("/me")
  public ResponseEntity<SuccessResponse<PageResponse<ArticleResponse>>> getMyArticles(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String tag,
      @RequestParam(required = false) Long categoryId,
      @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable,
      @AuthenticatedMember Long memberId) {
    return SuccessResponse.toOk(
        articleService.getArticles(pageable, memberId, keyword, tag, categoryId));
  }

  // === AI 서비스 ===

  @Operation(summary = "AI 문체 변환")
  @PostMapping("/transform-style")
  public ResponseEntity<SuccessResponse<StyleTransformResponse>> transformWritingStyle(
      @RequestBody @Valid StyleTransformRequest request, @AuthenticatedMember Long memberId) {
    return SuccessResponse.toOk(articleService.transformWritingStyle(request, memberId));
  }

  @Operation(summary = "AI 요약 조회")
  @GetMapping("/{articleId}/summary")
  public ResponseEntity<SuccessResponse<ArticleSummaryResponse>> getArticleSummary(
      @PathVariable Long articleId) {
    return SuccessResponse.toOk(articleService.getArticleSummary(articleId));
  }
}
