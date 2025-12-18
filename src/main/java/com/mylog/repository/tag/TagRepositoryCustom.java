package com.mylog.repository.tag;

import com.mylog.domain.entity.Article;
import java.util.List;

public interface TagRepositoryCustom  {
    List<String> findByArticle(Article article);
}
