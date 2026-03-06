<<<<<<<< HEAD:src/main/java/com/mylog/domain/tag/repository/TagRepositoryCustom.java
package com.mylog.domain.tag.repository;

import com.mylog.domain.article.entity.Article;
========
package com.mylog.tag.repository;

import com.mylog.article.entity.Article;
>>>>>>>> origin/main:domain/src/main/java/com/mylog/tag/repository/TagRepositoryCustom.java
import java.util.List;

public interface TagRepositoryCustom  {
    List<String> findByArticle(Article article);
}
