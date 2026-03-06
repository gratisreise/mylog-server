<<<<<<<< HEAD:src/main/java/com/mylog/domain/category/dto/CategoryUpdateRequest.java
package com.mylog.domain.category.dto;
========
package com.mylog.category.dto;
>>>>>>>> origin/main:api/src/main/java/com/mylog/category/dto/CategoryUpdateRequest.java

import jakarta.validation.constraints.NotBlank;

public record CategoryUpdateRequest(
    @NotBlank
    String categoryName
) { }
