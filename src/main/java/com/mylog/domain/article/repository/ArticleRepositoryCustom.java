package com.mylog.domain.article.repository;

import com.mylog.domain.article.dto.request.ArticleQueryParam;
import com.mylog.domain.article.dto.response.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleRepositoryCustom {

  Page<ArticleResponse> searchArticles(ArticleQueryParam params, Pageable pageable);
}
