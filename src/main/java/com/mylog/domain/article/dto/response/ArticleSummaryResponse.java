package com.mylog.domain.article.dto.response;

import com.mylog.common.enums.AnalyzeStatus;

public record ArticleSummaryResponse(Long articleId, String aiSummary, AnalyzeStatus status) {
  public static ArticleSummaryResponse of(Long articleId, String aiSummary, AnalyzeStatus status) {
    return new ArticleSummaryResponse(articleId, aiSummary, status);
  }
}
