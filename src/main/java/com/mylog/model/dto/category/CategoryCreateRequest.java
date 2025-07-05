package com.mylog.model.dto.category;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;


public record CategoryCreateRequest (
    @Length(min = 1, max = 12)
    @NotBlank
    String categoryName
){ }
