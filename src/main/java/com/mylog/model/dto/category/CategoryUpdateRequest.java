package com.mylog.model.dto.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryUpdateRequest(
    @NotBlank
    String categoryName
) { }
