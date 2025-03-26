package com.mylog.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleResponse {
    private String title;
    private String content;
    private String author;
    private String category;
    private String createdDate;
}
