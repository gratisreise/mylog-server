package com.mylog.domain.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryUpdateRequest(
    @NotBlank
    String categoryName
) { }
