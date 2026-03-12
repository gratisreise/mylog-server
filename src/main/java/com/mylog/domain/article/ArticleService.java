package com.mylog.domain.article;

import com.mylog.common.enums.WritingStyle;
import com.mylog.common.response.PageResponse;
import com.mylog.domain.article.dto.AiSummaryResult;
import com.mylog.domain.article.dto.request.ArticleCreateRequest;
import com.mylog.domain.article.dto.request.ArticleQueryParam;
import com.mylog.domain.article.dto.request.ArticleUpdateRequest;
import com.mylog.domain.article.dto.request.StyleTransformRequest;
import com.mylog.domain.article.dto.request.Temp;
import com.mylog.domain.article.dto.response.ArticleCreateResponse;
import com.mylog.domain.article.dto.response.ArticleResponse;
import com.mylog.domain.article.dto.response.ArticleSummaryResponse;
import com.mylog.domain.article.dto.response.StyleTransformResponse;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.service.AiService;
import com.mylog.domain.article.service.ArticleReader;
import com.mylog.domain.article.service.ArticleWriter;
import com.mylog.domain.member.entity.CustomWritingStyle;
import com.mylog.domain.member.service.CustomWritingStyleReader;
import com.mylog.external.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ArticleService {
  private final ArticleReader articleReader;
  private final ArticleWriter articleWriter;
  private final S3Service s3Service;
  private final AiService aiService;
  private final CustomWritingStyleReader customWritingStyleReader;

  public ArticleCreateResponse createArticle(
      ArticleCreateRequest request, Long memberId, MultipartFile file) {
    String imageUrl = s3Service.upload(file);
    Article article = articleWriter.create(request, memberId, imageUrl);
    return ArticleCreateResponse.from(article.getId());
  }

  public void updateArticle(
      ArticleUpdateRequest request, Long memberId, MultipartFile file, Long articleId) {
    String imageUrl = null;
    if (file != null && !file.isEmpty()) {
      imageUrl = s3Service.upload(file);
    }
    articleWriter.update(request, memberId, imageUrl, articleId);
  }

  public void deleteArticle(Long articleId, Long memberId) {
    articleWriter.delete(articleId, memberId);
  }

  public ArticleResponse getArticle(Long articleId) {
    return articleReader.getArticle(articleId);
  }

  /**
   * 통합 게시글 목록/검색 조회
   *
   * @param pageable 페이징 정보
   * @param memberId 회원 ID (null이면 전체, non-null이면 내 게시글)
   * @param keyword 제목 검색 키워드 (선택)
   * @param tag 태그 필터 (선택)
   * @param categoryId 카테고리 필터 (선택)
   * @return 게시글 목록
   */
  public PageResponse<ArticleResponse> getArticles(
      Pageable pageable, Long memberId, String keyword, String tag, Long categoryId) {
    ArticleQueryParam params = new ArticleQueryParam(memberId, keyword, tag, categoryId);
    return PageResponse.from(articleReader.getArticles(params, pageable));
  }

  // AI 문체 변환
  public StyleTransformResponse transformWritingStyle(
      StyleTransformRequest request, Long memberId) {
    String transformed;
    String styleName;

    if (request.customStyleId() != null) {
      // 커스텀 스타일 사용
      CustomWritingStyle customStyle =
          customWritingStyleReader.getByIdAndMemberId(request.customStyleId(), memberId);
      transformed = aiService.transformWithCustomStyle(request.content(), customStyle);
      styleName = customStyle.getName();
    } else {
      // 공통 스타일 사용
      WritingStyle style = request.writingStyle();
      transformed = aiService.transformWritingStyle(request.content(), style);
      styleName = style.name();
    }

    return StyleTransformResponse.of(transformed, styleName);
  }

  // AI 요약 조회
  public ArticleSummaryResponse getArticleSummary(Long articleId) {
    Article article = articleReader.getArticleById(articleId);
    return ArticleSummaryResponse.of(
        articleId, article.getAiSummary(), article.getAiSummaryStatus());
  }

  // AI 요약 조회
  public AiSummaryResult getArticleSummary(Temp temp) {
    return aiService.test(temp.content());
  }
}
