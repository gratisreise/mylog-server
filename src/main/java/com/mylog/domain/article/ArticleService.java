package com.mylog.domain.article;

import com.mylog.common.enums.WritingStyle;
import com.mylog.common.response.PageResponse;
import com.mylog.domain.article.dto.request.ArticleCreateRequest;
import com.mylog.domain.article.dto.request.ArticleSearchRequest;
import com.mylog.domain.article.dto.request.ArticleUpdateRequest;
import com.mylog.domain.article.dto.request.StyleTransformRequest;
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

  public PageResponse<ArticleResponse> getArticles(Pageable pageable) {
    return PageResponse.from(articleReader.getArticles(pageable));
  }

  public PageResponse<ArticleResponse> getArticles(Pageable pageable, Long memberId) {
    return PageResponse.from(articleReader.getArticles(pageable, memberId));
  }

  public PageResponse<ArticleResponse> searchArticles(
      String keyword, String tag, Long categoryId, Pageable pageable) {
    ArticleSearchRequest request =
        new ArticleSearchRequest(keyword, tag, categoryId, null, pageable);
    return PageResponse.from(articleReader.search(request));
  }

  public PageResponse<ArticleResponse> searchMyArticles(
      String keyword, String tag, Long categoryId, Pageable pageable, Long memberId) {
    ArticleSearchRequest request =
        new ArticleSearchRequest(keyword, tag, categoryId, memberId, pageable);
    return PageResponse.from(articleReader.search(request));
  }

  // AI 문체 변환
  public StyleTransformResponse transformWritingStyle(StyleTransformRequest request, Long memberId) {
    String transformed;
    String styleName;

    if (request.customStyleId() != null) {
      // 커스텀 스타일 사용
      CustomWritingStyle customStyle = customWritingStyleReader.getByIdAndMemberId(request.customStyleId(), memberId);
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
}
