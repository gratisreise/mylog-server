package com.mylog.model.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateRequest {
    @Length(min=2, max=200)
    @NotBlank
    private String content;

    @NotBlank
    private long parentCommentId;
}
