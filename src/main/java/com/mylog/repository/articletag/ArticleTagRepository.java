package com.mylog.repository.articletag;

import com.mylog.domain.entity.Article;
import com.mylog.domain.entity.ArticleTag;
import com.mylog.domain.entity.ArticleTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, ArticleTagId>, ArticleTagRepositoryCustom {
    void deleteByArticle(Article article);
}
