package com.mylog.tag.repository;

import com.mylog.article.entity.Article;
import java.util.List;

public interface TagRepositoryCustom  {
    List<String> findByArticle(Article article);
}
