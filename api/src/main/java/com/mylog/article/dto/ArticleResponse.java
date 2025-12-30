package com.mylog.article.dto;

import com.mylog.article.entity.Article;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record ArticleResponse(
    Long id,
    String title,
    String content,
    String author,
    String category,
    String articleImg,
    List<String> tags,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

    public static ArticleResponse of(Article article, List<String> tags){
        return ArticleResponse.builder()
            .id(article.getId())
            .title(article.getTitle())
            .content(article.getContent())
            .author(article.getMember().getMemberName())
            .category(article.getCategory().getCategoryName())
            .articleImg(article.getArticleImg())
            .tags(tags)
            .createdAt(article.getCreatedAt())
            .updatedAt(article.getUpdatedAt())
            .build();
    }


}