package com.mylog.domain.category.dto;


import com.mylog.domain.category.Category;

public record CategoryResponse(Long id, String categoryName, Long memberId) {
    public CategoryResponse (Category category) {
        this(category.getId(),
            category.getCategoryName(),
            category.getMember().getId());
    }
}
