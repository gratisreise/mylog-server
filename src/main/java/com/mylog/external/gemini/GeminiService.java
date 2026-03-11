package com.mylog.external.gemini;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiService {

  private final Client client;

  public String gemini(String prompt) {
    try {
      GenerateContentResponse response =
          client.models.generateContent("gemini-2.5-flash", prompt, null);
      return response.text();
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
    }
  }
}
