package com.mylog.repository.tag;

import com.mylog.model.entity.Article;
import com.mylog.model.entity.Tag;
import java.util.List;

public interface TagRepositoryCustom  {
    List<Tag> findByArticle(Article article);
}
