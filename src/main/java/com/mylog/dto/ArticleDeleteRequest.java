package com.mylog.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleDeleteRequest {
    private Long id;
    private String author;
}
