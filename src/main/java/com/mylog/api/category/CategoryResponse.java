package com.mylog.api.category;

import com.mylog.domain.entity.Category;


public record CategoryResponse(Long id, String categoryName, Long memberId) {
    public CategoryResponse (Category category) {
        this(category.getId(),
            category.getCategoryName(),
            category.getMember().getId());
    }
}
