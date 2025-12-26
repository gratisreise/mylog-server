package com.mylog.api.article.dto;

import com.mylog.api.article.entity.Article;
import com.mylog.api.category.entity.Category;
import com.mylog.api.member.entity.Member;
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

) {
    public Article toEntity(Member member, Category category, String imageUrl){
        return Article.builder()
            .member(member)
            .category(category)
            .title(this.title)
            .content(this.content)
            .articleImg(imageUrl)
            .build();
    }
}
