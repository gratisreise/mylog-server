package com.mylog.api.tag.repository;

import com.mylog.api.article.entity.Article;
import java.util.List;

public interface TagRepositoryCustom  {
    List<String> findByArticle(Article article);
}
