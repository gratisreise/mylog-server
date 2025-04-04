package com.mylog.dto.category;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class CategoryUpdateRequest {
    private Long id;
    private String categoryName;
}
