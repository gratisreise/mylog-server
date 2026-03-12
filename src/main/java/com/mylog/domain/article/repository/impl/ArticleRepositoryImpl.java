package com.mylog.domain.article.repository.impl;

import com.mylog.domain.article.dto.request.ArticleQueryParam;
import com.mylog.domain.article.dto.response.ArticleResponse;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.entity.QArticle;
import com.mylog.domain.article.entity.QArticleTag;
import com.mylog.domain.article.entity.QTag;
import com.mylog.domain.article.repository.ArticleRepositoryCustom;
import com.mylog.domain.category.QCategory;
import com.mylog.domain.member.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;

  @Override
  public Page<ArticleResponse> searchArticles(ArticleQueryParam params, Pageable pageable) {
    QArticle article = QArticle.article;
    QMember member = QMember.member;
    QCategory category = QCategory.category;
    QArticleTag articleTag = QArticleTag.articleTag;
    QTag tag = QTag.tag;

    BooleanBuilder builder = new BooleanBuilder();

    if (params.hasMemberFilter()) {
      builder.and(article.member.id.eq(params.memberId()));
    }

    if (params.hasKeyword()) {
      builder.and(article.title.lower().like("%" + params.keyword().toLowerCase() + "%"));
    }

    if (params.hasCategory()) {
      builder.and(article.category.id.eq(params.categoryId()));
    }

    if (params.hasTag()) {
      List<Long> articleIds = findArticleIdsByTag(params.tag(), params.memberId());
      if (articleIds.isEmpty()) {
        return new PageImpl<>(List.of(), pageable, 0L);
      }
      builder.and(article.id.in(articleIds));
    }

    List<Article> articles =
        jpaQueryFactory
            .selectFrom(article)
            .join(article.member, member)
            .fetchJoin()
            .join(article.category, category)
            .fetchJoin()
            .where(builder)
            .orderBy(article.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long count = jpaQueryFactory.select(article.count()).from(article).where(builder).fetchOne();

    List<ArticleResponse> responses = articles.stream().map(ArticleResponse::from).toList();

    return new PageImpl<>(responses, pageable, count != null ? count : 0L);
  }

  private List<Long> findArticleIdsByTag(String tagName, Long memberId) {
    QArticleTag articleTag = QArticleTag.articleTag;
    QTag tag = QTag.tag;
    QArticle article = QArticle.article;

    var query =
        jpaQueryFactory
            .select(articleTag.article.id)
            .from(articleTag)
            .join(articleTag.tag, tag)
            .join(articleTag.article, article)
            .where(tag.tagName.eq(tagName));

    if (memberId != null) {
      query.where(tag.tagName.eq(tagName).and(article.member.id.eq(memberId)));
    }

    return query.fetch();
  }
}
