package com.mylog.domain.article.dto.request;

import com.mylog.domain.article.entity.Article;
import com.mylog.domain.category.Category;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record ArticleUpdateRequest(
    @NotBlank @Length(min = 2, max = 30) String title,
    @NotBlank @Length(min = 2, max = 3000) String content,
    @NotBlank @Length(min = 2, max = 12) String category,
    @NotBlank String author) {
  public Article toEntity(String image, Category category) {
    return Article.builder()
        .title(title)
        .content(content)
        .articleImg(image)
        .category(category)
        .build();
  }
}
