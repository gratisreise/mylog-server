package com.mylog.domain.article.service;

import com.mylog.common.enums.WritingStyle;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.repository.ArticleRepository;
import com.mylog.domain.member.entity.CustomWritingStyle;
import com.mylog.external.gemini.GeminiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {
  private final GeminiService geminiService;
  private final ArticleRepository articleRepository;

  // 동기: 문체 변환 (GeminiService 에러는 이미 처리됨)
  public String transformWritingStyle(String content, WritingStyle style) {
    log.info("문체 변환 시작: style={}", style);
    String prompt = style.generatePrompt(content);
    return geminiService.gemini(prompt);
  }

  // 동기: 커스텀 문체 변환
  public String transformWithCustomStyle(String content, CustomWritingStyle customStyle) {
    log.info("커스텀 문체 변환 시작: styleId={}, name={}", customStyle.getId(), customStyle.getName());
    String prompt = customStyle.generatePrompt(content);
    return geminiService.gemini(prompt);
  }

  // 비동기: 요약 생성
  @Async
  @Transactional
  public void generateSummaryAsync(Long articleId) {
    log.info("비동기 요약 생성 시작: articleId={}", articleId);

    Article article =
        articleRepository
            .findById(articleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

    try {
      String prompt = buildSummaryPrompt(article.getContent());
      String summary = geminiService.gemini(prompt);
      article.updateAiSummary(summary);
      log.info("요약 생성 완료: articleId={}", articleId);
    } catch (BusinessException e) {
      // GeminiService에서 던진 API 에러
      log.error("요약 생성 실패 (API 에러): articleId={}", articleId);
      article.markAiSummaryFailed();
    }
  }

  private String buildSummaryPrompt(String content) {
    return String.format("다음 블로그 게시글의 핵심 내용을 3-5문장으로 요약해줘.\n\n내용: %s", content);
  }
}
