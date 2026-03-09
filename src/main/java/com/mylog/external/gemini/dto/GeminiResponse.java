package com.mylog.external.gemini.dto;

import java.util.List;

public record GeminiResponse(List<Candidate> candidates) {}
