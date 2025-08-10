package com.mylog.repository.articletag;

import com.mylog.model.entity.Article;
import com.mylog.model.entity.ArticleTag;
import com.mylog.model.entity.Tag;
import com.mylog.model.entity.compositekey.ArticleTagId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, ArticleTagId>, ArticleTagRepositoryCustom {
    void deleteByArticle(Article article);
}
