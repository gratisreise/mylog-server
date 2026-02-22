<<<<<<<< HEAD:src/main/java/com/mylog/domain/comment/dto/CommentUpdateRequest.java
package com.mylog.domain.comment.dto;
========
package com.mylog.comment.dto;
>>>>>>>> origin/main:api/src/main/java/com/mylog/comment/dto/CommentUpdateRequest.java

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CommentUpdateRequest(
    @Length(min=2, max=200) @NotBlank String content
) { }
