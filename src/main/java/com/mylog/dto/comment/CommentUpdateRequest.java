package com.mylog.dto.comment;

import jakarta.validation.Valid;
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
public class CommentUpdateRequest {
    @Length(min=2, max=200)
    @NotBlank
    private String content;

    @NotBlank
    private Long commentId;
}
