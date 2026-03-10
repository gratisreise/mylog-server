package com.mylog.domain.article.dto.response;

public record StyleTransformResponse(String transformedContent, String writingStyle) {
  public static StyleTransformResponse of(String transformedContent, String writingStyle) {
    return new StyleTransformResponse(transformedContent, writingStyle);
  }
}
