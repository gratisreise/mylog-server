package com.mylog.domain.article;

import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.reader.ArticleReader;
import com.mylog.domain.article.service.ArticleWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleReader articleReader;
    private final ArticleWriter articleWriter;

    public void createArticle(ArticleCreateRequest request, Long memberId, String imageUrl) {

    }
}
