package com.mylog.model.dto.category;

import com.mylog.model.entity.Category;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryResponse {
    private Long id;
    private String categoryName;
    private Long memberId;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
            .id(category.getId())
            .categoryName(category.getCategoryName())
            .memberId(category.getMember().getId())
            .build();
    }
}
