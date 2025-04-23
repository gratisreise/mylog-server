package com.mylog.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Getter
@Service
@AllArgsConstructor
@NoArgsConstructor
public class CategoryUpdateRequest {
    private Long categoryId;
    private String categoryName;
    private Long memberId;
}
