package com.mylog.article.repository;


import com.mylog.article.entity.QArticle;
import com.mylog.article.entity.QArticleTag;
import com.mylog.article.projections.ArticleProjection;
import com.mylog.category.entity.QCategory;
import com.mylog.member.entity.QMember;
import com.mylog.tag.entity.QTag;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
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

    // 태그 이름 집합
    private final String TAG_FUNCTION = "STRING_AGG({0}, ',' ORDER_BY {0}) ASC";
//    private Long id;
//    private Member member;
//    private Category category;
//    private String content;
//    private String articleImg;
//    private List<String> tags;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
    @Override
    public Page<ArticleProjection> findAllCustom(Pageable pageable) {
        QArticle article = QArticle.article;
        QArticleTag articleTag = QArticleTag.articleTag;
        QTag tag = QTag.tag;

        List<ArticleProjection> articleProjections =
            findAll(pageable, article, tag, articleTag);
        long total = countTotal(article);

        return new PageImpl<>(articleProjections, pageable, total);
    }

    private long countTotal(QArticle article){
        return queryFactory
            .select(article.count())
            .from(article)
            .fetchOne();
    }

    private List<ArticleProjection> findAll(Pageable pageable, QArticle article, QTag tag,
        QArticleTag articleTag) {
        return queryFactory.select(Projections.constructor(
                ArticleProjection.class,
                article.id,
                article.title,
                article.member.memberName,
                article.category.categoryName,
                article.content,
                article.articleImg,
                Expressions.stringTemplate(TAG_FUNCTION, tag.tagName),
                article.createdAt,
                article.updatedAt)
            ).from(article)
            .leftJoin(articleTag).on(article.id.eq(articleTag.article.id))
            .leftJoin(tag).on(articleTag.tag.id.eq(tag.id))
            .groupBy(article.id)
            .orderBy(article.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();
    }
}
