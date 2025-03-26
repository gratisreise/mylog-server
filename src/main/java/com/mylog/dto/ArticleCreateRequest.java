package com.mylog.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ArticleCreateRequest {
    private String title;
    private String content;
    private String memberId;
    private String category;
}
