package com.mylog.domain.tag.repository;

import com.mylog.domain.article.entity.Article;
import java.util.List;

public interface TagRepositoryCustom  {
    List<String> findByArticle(Article article);
}
