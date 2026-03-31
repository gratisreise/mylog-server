package com.mylog.domain.article.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mylog.common.enums.WritingStyle;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.article.dto.AiSummaryResult;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.repository.ArticleRepository;
import com.mylog.domain.member.entity.CustomWritingStyle;
import com.mylog.external.gemini.GeminiService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {
  private final GeminiService geminiService;
  private final ArticleRepository articleRepository;
  private final TagWriter tagWriter;
  private final ObjectMapper objectMapper;
  private final RetryTemplate aiRetryTemplate;

  // 동기: 문체 변환 (GeminiService 에러는 이미 처리됨)
  public String transformWritingStyle(String content, WritingStyle style) {
    String prompt = style.generatePrompt(content);
    return geminiService.gemini(prompt);
  }

  // 동기: 커스텀 문체 변환
  public String transformWithCustomStyle(String content, CustomWritingStyle customStyle) {
    String prompt = customStyle.generatePrompt(content);
    return geminiService.gemini(prompt);
  }

  public AiSummaryResult test(String content) {
    String prompt = buildSummaryPrompt(content);
    String response = geminiService.gemini(prompt);

    // JSON 파싱
    AiSummaryResult result = parseAiSummaryResponse(response);
    return result;
  }

  // 비동기: 요약 생성 + 태그 자동 생성
  @Async("threadPoolTaskExecutor")
  @Transactional
  public void generateSummaryAsync(Long articleId) {

    Article article =
        articleRepository
            .findById(articleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

    try {
      String prompt = buildSummaryPrompt(article.getContent());
      String response =
          aiRetryTemplate.execute(
              context -> {
                log.info("AI 요약 생성 시도 (attempt: {})", context.getRetryCount() + 1);
                return geminiService.gemini(prompt);
              });

      // JSON 파싱
      AiSummaryResult result = parseAiSummaryResponse(response);

      // 요약 저장
      article.updateAiSummary(result.summary());

      // AI 태그 저장 (비동기)
      if (result.tags() != null && !result.tags().isEmpty()) {
        tagWriter.saveAiTags(result.tags(), article);
      }
    } catch (BusinessException e) {
      // GeminiService에서 던진 API 에러
      article.markAiSummaryFailed();
    } catch (Exception e) {
      log.error("AI 요약 생성 중 오류", e);
      article.markAiSummaryFailed();
    }
  }

  private String buildSummaryPrompt(String content) {
    return String.format(
        """
        # Role
        너는 온라인 콘텐츠를 분석하고 핵심 정보를 정리하는 전문 콘텐츠 에디터다.

        # Task
        입력된 게시글을 분석하여 다음 두 가지 작업을 수행해줘.
        1. 핵심 내용을 한 문단으로 요약할 것.
        2. 게시글의 주제와 맥락을 관통하는 키워드 태그를 5개 추출할 것.

        # Constraint
        - 요약은 원문의 톤을 유지하되 간결하게 작성할 것.
        - 태그는 '#'을 붙이지 않은 단어 형태로 추출할 것.
        - 태그는 각각 10자 이내로 작성할 것.
        - 출력은 반드시 아래 JSON 형식을 엄격히 지킬 것.
        - JSON 외 다른 텍스트는 포함하지 말 것.

        # Output Format (JSON)
        {
          "summary": "요약된 문장들",
          "tags": ["태그1", "태그2", "태그3", "태그4", "태그5"]
        }

        # Input Content
        %s
        """,
        content);
  }

  private AiSummaryResult parseAiSummaryResponse(String response) {
    try {
      String json = response;
      // 마크다운 코드 블록 제거
      if (json.contains("```json")) {
        json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
      } else if (json.contains("```")) {
        json = json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1);
      }
      return objectMapper.readValue(json, AiSummaryResult.class);
    } catch (Exception e) {
      log.warn("JSON 파싱 실패, 원본 응답을 summary로 사용: {}", response);
      return new AiSummaryResult(response, List.of());
    }
  }
}
