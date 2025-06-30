package com.mylog.repository;

import com.mylog.entity.Article;
import com.mylog.entity.ArticleTag;
import com.mylog.entity.compositekey.ArticleTagId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, ArticleTagId> {

    List<ArticleTag> findByArticle(Article article);

    void deleteByArticle(Article article);
}
