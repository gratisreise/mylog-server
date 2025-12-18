package com.mylog.api.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryUpdateRequest(
    @NotBlank
    String categoryName
) { }
