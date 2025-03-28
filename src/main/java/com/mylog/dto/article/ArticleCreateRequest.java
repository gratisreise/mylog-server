package com.mylog.dto.article;

import lombok.Getter;

@Getter
public class ArticleCreateRequest {
    private String title;
    private String content;
    private String memberId;
    private String category;
}
