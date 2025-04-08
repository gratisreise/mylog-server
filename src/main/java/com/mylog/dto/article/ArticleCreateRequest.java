package com.mylog.dto.article;

import java.util.List;
import lombok.Getter;

@Getter
public class ArticleCreateRequest {
    private String title;
    private String content;
    private String memberId;
    private String category;
    private List<String> tags;
}
