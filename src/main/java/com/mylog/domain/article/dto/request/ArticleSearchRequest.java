package com.mylog.domain.article.dto.request;

import org.springframework.data.domain.Pageable;

public record ArticleSearchRequest(
    String keyword,
    String tag,
    Long categoryId,
    Long memberId,
    Pageable pageable
) {
    public boolean hasKeyword() {
        return keyword != null && !keyword.isBlank();
    }

    public boolean hasTag() {
        return tag != null && !tag.isBlank();
    }

    public boolean hasCategory() {
        return categoryId != null;
    }

    public boolean isMyArticles() {
        return memberId != null;
    }
}
