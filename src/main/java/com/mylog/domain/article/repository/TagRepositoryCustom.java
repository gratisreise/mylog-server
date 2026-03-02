package com.mylog.domain.article.repository;

import com.mylog.domain.article.entity.Article;
import java.util.List;

public interface TagRepositoryCustom  {
    List<String> findByArticle(Article article);
}
