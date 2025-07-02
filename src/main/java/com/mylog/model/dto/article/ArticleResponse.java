package com.mylog.model.dto.article;

import com.mylog.model.entity.Article;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@ToString
public class ArticleResponse {
    private Long id;
    private String title;
    private String content;
    private String author;
    private String category;
    private LocalDateTime createdAt;

    public ArticleResponse(Article article, String author, String category) {
        this.id = article.getId();
        this.title = article.getTitle();
        this.content = article.getContent();
        this.author = author;
        this.category = category;
        this.createdAt = article.getCreatedAt();
    }

    public static ArticleResponse from(Article article) {
        return ArticleResponse.builder()
            .id(article.getId())
            .title(article.getTitle())
            .content(article.getContent())
            .author(article.getMember().getNickname())
            .category(article.getCategory().getCategoryName())
            .createdAt(article.getCreatedAt())
            .build();
    }
}
