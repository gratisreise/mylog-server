<<<<<<<< HEAD:src/main/java/com/mylog/domain/article/dto/ArticleUpdateRequest.java
package com.mylog.domain.article.dto;
========
package com.mylog.article.dto;
>>>>>>>> origin/main:api/src/main/java/com/mylog/article/dto/ArticleUpdateRequest.java

import com.mylog.article.entity.Article;
import com.mylog.category.entity.Category;
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
     List<String> tagNames
) {
    public Article toEntity(String image, Category category){
        return Article.builder()
            .title(title)
            .content(content)
            .articleImg(image)
            .category(category)
            .build();
    }
}
