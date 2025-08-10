package com.mylog.repository.article;

import com.mylog.model.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleRepositoryCustom  {

    Page<Article> findAllByTagName(String tagName, Pageable pageable);
}
