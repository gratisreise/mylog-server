package com.mylog.model.dto.article;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ArticleCreateRequest (
    @NotBlank String title,
    @NotBlank String content,
    @NotBlank String category,
    List<String> tags
) {}
