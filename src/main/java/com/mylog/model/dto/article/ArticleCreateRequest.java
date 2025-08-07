package com.mylog.model.dto.article;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.hibernate.validator.constraints.Length;

public record ArticleCreateRequest (
    @NotBlank
    @Length(min = 5, max = 30)
    String title,
    @NotBlank
    @Length(min = 10, max = 3000)
    String content,
    @NotBlank
    String category,
    List<String> tags
) {}
