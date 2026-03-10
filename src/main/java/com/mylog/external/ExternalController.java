package com.mylog.external;

import com.mylog.common.response.SuccessResponse;
import com.mylog.external.gemini.GeminiService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/external")
@Validated
public class ExternalController {

  private final GeminiService geminiService;

  @PostMapping("/gemini")
  @Operation(summary = "AI 텍스트 생성 테스트")
  public ResponseEntity<SuccessResponse<String>> generateContent(
      @RequestBody @Validated AiGenerateRequest request) {
    String result = geminiService.gemini(request.prompt());
    return SuccessResponse.toOk(result);
  }

  public record AiGenerateRequest(@NotBlank(message = "프롬프트는 필수입니다.") String prompt) {}
}
