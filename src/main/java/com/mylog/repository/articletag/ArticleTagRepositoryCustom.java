package com.mylog.repository.articletag;


import com.mylog.model.entity.Article;
import com.mylog.model.entity.Tag;
import java.util.List;

public interface ArticleTagRepositoryCustom {

    List<Tag> findByArticle(Article article);
}
