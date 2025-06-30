package com.mylog.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class ArticleUpdateRequest {

    @NotNull
    private Long id;

    @NotBlank
    @Length(min = 2, max = 30)
    private String title;

    @NotBlank
    @Length(min = 2, max = 3000)
    private String content;

    @NotBlank
    @Length(min = 2, max = 12)
    private String category;

    @NotBlank
    private String author;

    private List<String> tags;

}
