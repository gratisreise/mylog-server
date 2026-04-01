package com.mylog.domain.article.repository.impl;

import com.mylog.domain.article.dto.request.ArticleQueryParam;
import com.mylog.domain.article.dto.response.ArticleResponse;
import com.mylog.domain.article.entity.QArticle;
import com.mylog.domain.article.entity.QArticleTag;
import com.mylog.domain.article.entity.QTag;
import com.mylog.domain.article.repository.ArticleRepositoryCustom;
import com.mylog.domain.category.QCategory;
import com.mylog.domain.member.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.group.GroupBy;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    BooleanBuilder builder = buildFilterConditions(params, article, articleTag, tag);

    // Phase 1: 페이징된 게시글 ID 조회
    List<Long> articleIds =
        jpaQueryFactory
            .select(article.id)
            .from(article)
            .join(article.member, member)
            .join(article.category, category)
            .where(builder)
            .orderBy(article.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    if (articleIds.isEmpty()) {
      return new PageImpl<>(List.of(), pageable, 0L);
    }

    Long count = jpaQueryFactory.select(article.count()).from(article).where(builder).fetchOne();

    // Phase 2: GroupBy.transform으로 게시글 + 태그 프로젝션
    Map<Long, ArticleResponse> resultMap =
        jpaQueryFactory
            .from(article)
            .join(article.member, member)
            .join(article.category, category)
            .leftJoin(articleTag)
            .on(articleTag.article.eq(article))
            .leftJoin(articleTag.tag, tag)
            .where(article.id.in(articleIds))
            .orderBy(article.createdAt.desc())
            .transform(
                GroupBy.groupBy(article.id)
                    .as(
                        Projections.constructor(
                            ArticleResponse.class,
                            article.id,
                            article.title,
                            article.content,
                            member.nickname,
                            category.categoryName,
                            article.articleImg,
                            GroupBy.list(tag.tagName),
                            article.createdAt,
                            article.updatedAt)));

    List<ArticleResponse> responses =
        new ArrayList<>(resultMap.values()).stream().map(this::filterNullTags).toList();

    return new PageImpl<>(responses, pageable, count != null ? count : 0L);
  }

  private BooleanBuilder buildFilterConditions(
      ArticleQueryParam params, QArticle article, QArticleTag articleTag, QTag tag) {
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
        builder.and(article.id.eq(-1L));
      } else {
        builder.and(article.id.in(articleIds));
      }
    }

    return builder;
  }

  private List<Long> findArticleIdsByTag(String tagName, Long memberId) {
    QArticleTag articleTag = QArticleTag.articleTag;
    QTag tag = QTag.tag;
    QArticle article = QArticle.article;

    return jpaQueryFactory
        .select(articleTag.article.id)
        .from(articleTag)
        .join(articleTag.tag, tag)
        .join(articleTag.article, article)
        .where(tag.tagName.eq(tagName), memberId != null ? article.member.id.eq(memberId) : null)
        .fetch();
  }

  private ArticleResponse filterNullTags(ArticleResponse response) {
    if (response.tags() == null || response.tags().stream().allMatch(Objects::nonNull)) {
      return response;
    }
    List<String> filteredTags = response.tags().stream().filter(Objects::nonNull).toList();
    return ArticleResponse.builder()
        .id(response.id())
        .title(response.title())
        .content(response.content())
        .author(response.author())
        .category(response.category())
        .articleImg(response.articleImg())
        .tags(filteredTags)
        .createdAt(response.createdAt())
        .updatedAt(response.updatedAt())
        .build();
  }
}
