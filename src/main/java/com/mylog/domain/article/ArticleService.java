package com.mylog.domain.article;

import com.mylog.domain.article.dto.request.ArticleCreateRequest;
import com.mylog.domain.article.reader.ArticleReader;
import com.mylog.domain.article.writer.ArticleWriter;
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
