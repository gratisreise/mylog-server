package com.mylog.repository.tag;

import com.mylog.model.entity.Article;
import com.mylog.model.entity.QArticleTag;
import com.mylog.model.entity.QTag;
import com.mylog.model.entity.Tag;
import com.mylog.repository.notification.NotificationRepositoryCustom;
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
