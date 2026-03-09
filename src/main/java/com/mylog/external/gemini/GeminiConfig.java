package com.mylog.external.gemini;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GeminiConfig {

  @Bean
  public Client geminiClient() {
    return Client.builder().apiKey(System.getenv("GEMINI_API_KEY")).build();
  }
}
