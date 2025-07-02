package com.mylog.repository;

import com.mylog.model.entity.Article;
import com.mylog.model.entity.ArticleTag;
import com.mylog.model.entity.compositekey.ArticleTagId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, ArticleTagId> {

    List<ArticleTag> findByArticle(Article article);

    void deleteByArticle(Article article);
}
