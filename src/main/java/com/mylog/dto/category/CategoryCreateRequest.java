package com.mylog.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class CategoryCreateRequest {
    @Length(min = 1, max = 12)
    @NotBlank
    private String categoryName;
}
