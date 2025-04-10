package com.mylog.dto.comment;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class CommentUpdateRequest {
    @Length(min=2, max=200)
    private String content;
}
