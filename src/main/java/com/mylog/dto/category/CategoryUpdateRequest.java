package com.mylog.dto.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
@AllArgsConstructor
@NoArgsConstructor
public class CategoryUpdateRequest {
    private String categoryName;
}
