package com.mylog.dto.category;

import com.mylog.entity.Category;
import com.mylog.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Builder
public class CategoryResponse {
    private Long id;
    private String categoryName;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
            .id(category.getId())
            .categoryName(category.getCategoryName())
            .build();
    }
}
