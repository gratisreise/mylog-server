package com.mylog.model.dto.category;

import com.mylog.model.entity.Category;


public record CategoryResponse(Long id, String categoryName, Long memberId) {
    public CategoryResponse (Category category) {
        this(category.getId(),
            category.getCategoryName(),
            category.getMember().getId());
    }
}
