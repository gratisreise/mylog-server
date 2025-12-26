package com.mylog.api.article.repository;

import com.mylog.api.article.entity.Article;
import com.mylog.api.article.entity.ArticleTag;
import com.mylog.api.article.entity.ArticleTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, ArticleTagId>, ArticleTagRepositoryCustom {
    void deleteByArticle(Article article);
}
