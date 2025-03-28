package com.mylog.dto.article;

import lombok.Getter;
import org.hibernate.validator.constraints.Length;

@Getter
public class ArticleUpdateRequest {
    private Long id;
    @Length(min = 2, max = 30)
    private String title;
    @Length(min = 2, max = 3000)
    private String content;
    @Length(min = 2, max = 12)
    private String category;

    private String author;
}
