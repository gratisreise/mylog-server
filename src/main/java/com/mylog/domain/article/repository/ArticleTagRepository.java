package com.mylog.domain.article.repository;

import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.entity.ArticleTag;
import com.mylog.domain.article.entity.ArticleTagId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, ArticleTagId> {
  void deleteByArticle(Article article);

  List<ArticleTag> findAllByArticleId(Long articleId);
}
