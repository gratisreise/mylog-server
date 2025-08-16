package com.mylog.model.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    List<String> tags
) {}
