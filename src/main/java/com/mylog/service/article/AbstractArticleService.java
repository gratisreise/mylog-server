package com.mylog.service.article;

import com.mylog.dto.ArticleCreateRequest;
import com.mylog.dto.ArticleResponse;
import com.mylog.dto.classes.CustomUser;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractArticleService implements ArticleService {

    @Override
    public List<ArticleResponse> getArticles() {
        return List.of();
    }

    @Override
    public List<ArticleResponse> getArticles(String keyword) {
        return List.of();
    }

    @Override
    public ArticleResponse getArticle(Long id) {
        return null;
    }
}
