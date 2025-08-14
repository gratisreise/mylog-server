package com.mylog.repository.article;

import com.mylog.model.entity.Article;
import com.mylog.model.entity.QArticle;
import com.mylog.model.entity.QArticleTag;
import com.mylog.model.entity.QTag;
import com.mylog.repository.member.MemberRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
@Slf4j
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Article> findAllByTagName(String tagName, Pageable pageable) {
        QArticle article = QArticle.article;
        QArticleTag articleTag = QArticleTag.articleTag;
        QTag tag = QTag.tag;

        List<Article> content = queryFactory
            .select(article)
            .from(articleTag)
            .join(articleTag.article, article)
            .join(articleTag.tag, tag)
            .where(tag.tagName.eq(tagName))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(article.count())
            .from(articleTag)
            .join(articleTag.article, article)
            .join(articleTag.tag, tag)
            .where(tag.tagName.eq(tagName))
            .fetchOne();

        total = total == null ? 0 : total;

        return new PageImpl<>(content, pageable, total);
    }

}
