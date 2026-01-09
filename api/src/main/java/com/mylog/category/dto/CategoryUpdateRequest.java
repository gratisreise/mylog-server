package com.mylog.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryUpdateRequest(
    @NotBlank
    String categoryName
) { }
