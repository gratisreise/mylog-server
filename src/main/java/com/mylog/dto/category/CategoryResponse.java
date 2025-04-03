package com.mylog.dto.category;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
public class CategoryResponse {
    private Long id;
    private String categoryName;
}
