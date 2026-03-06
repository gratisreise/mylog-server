<<<<<<<< HEAD:src/main/java/com/mylog/domain/tag/repository/TagRepositoryImpl.java
<<<<<<<< HEAD:src/main/java/com/mylog/domain/tag/repository/TagRepositoryImpl.java
package com.mylog.domain.tag.repository;

import com.mylog.domain.article.entity.Article;
import com.mylog.model.entity.QArticleTag;
import com.mylog.model.entity.QTag;
========
package com.mylog.tag.repository;

========
package com.mylog.tag.repository;

>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:domain/src/main/java/com/mylog/tag/repository/TagRepositoryImpl.java
import com.mylog.article.entity.Article;

import com.mylog.article.entity.QArticleTag;
import com.mylog.tag.entity.QTag;
<<<<<<<< HEAD:src/main/java/com/mylog/domain/tag/repository/TagRepositoryImpl.java
>>>>>>>> origin/main:domain/src/main/java/com/mylog/tag/repository/TagRepositoryImpl.java
========
>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:domain/src/main/java/com/mylog/tag/repository/TagRepositoryImpl.java
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<String> findByArticle(Article article) {
        QArticleTag articleTag = QArticleTag.articleTag;
        QTag tag = QTag.tag;

        return  queryFactory
            .select(tag.tagName)
            .from(articleTag)
            .join(articleTag.tag, tag)
            .where(articleTag.article.eq(article))
            .fetch();
    }
}
