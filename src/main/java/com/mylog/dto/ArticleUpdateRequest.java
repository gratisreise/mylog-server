package com.mylog.dto;

import lombok.Getter;

@Getter
public class ArticleUpdateRequest {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String author;
}
