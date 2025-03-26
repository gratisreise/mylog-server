package com.mylog.dto;

import com.mylog.entity.Article;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ArticleResponse {
    private String title;
    private String content;
    private String author;
    private String category;
    private LocalDateTime createdDate;

    public ArticleResponse(Article article, String author, String category) {
        this.title = article.getTitle();
        this.content = article.getContent();
        this.author = author;
        this.category = category;
        this.createdDate = article.getCreatedAt();
    }
}
