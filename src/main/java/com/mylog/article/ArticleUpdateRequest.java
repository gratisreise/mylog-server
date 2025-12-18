package com.mylog.article;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.hibernate.validator.constraints.Length;

public record ArticleUpdateRequest(
    @NotBlank
    @Length(min = 2, max = 30)
     String title,
    @NotBlank
    @Length(min = 2, max = 3000)
     String content,
    @NotBlank
    @Length(min = 2, max = 12)
     String category,
    @NotBlank
     String author,
     List<String> tags
) { }
