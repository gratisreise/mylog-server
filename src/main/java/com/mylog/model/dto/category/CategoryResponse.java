package com.mylog.model.dto.category;

import com.mylog.model.entity.Category;


public record CategoryResponse(Long id, String categoryName, Long memberId) {
    public CategoryResponse (Category category, long memberId) {
        this(category.getId(),
            category.getCategoryName(),
            memberId);
    }
}
