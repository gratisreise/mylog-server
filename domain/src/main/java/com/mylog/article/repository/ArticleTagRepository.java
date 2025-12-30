package com.mylog.article.repository;

import com.mylog.article.entity.Article;
import com.mylog.article.entity.ArticleTag;
import com.mylog.article.entity.ArticleTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, ArticleTagId>, ArticleTagRepositoryCustom {
    void deleteByArticle(Article article);
}
