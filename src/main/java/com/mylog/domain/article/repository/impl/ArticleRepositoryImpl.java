package com.mylog.domain.article.repository.impl;

import com.mylog.domain.article.dto.response.ArticleResponse;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.entity.QArticle;
import com.mylog.domain.article.entity.QArticleTag;
import com.mylog.domain.article.entity.QTag;
import com.mylog.domain.article.repository.ArticleRepositoryCustom;
import com.mylog.domain.category.QCategory;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.entity.QMember;
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
  public Page<ArticleResponse> findAllCustom(Pageable pageable) {
    QArticle article = QArticle.article;
    QMember member = QMember.member;
    QCategory category = QCategory.category;

    List<Article> articles =
        jpaQueryFactory
            .selectFrom(article)
            .join(article.member, member)
            .fetchJoin()
            .join(article.category, category)
            .fetchJoin()
            .orderBy(article.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long count = jpaQueryFactory.select(article.count()).from(article).fetchOne();

    List<ArticleResponse> responses =
        articles.stream().map(a -> new ArticleResponse(a, List.of())).toList();

    return new PageImpl<>(responses, pageable, count != null ? count : 0L);
  }

  @Override
  public Page<ArticleResponse> findMineByMember(Member memberParam, Pageable pageable) {
    QArticle article = QArticle.article;
    QMember member = QMember.member;
    QCategory category = QCategory.category;

    List<Article> articles =
        jpaQueryFactory
            .selectFrom(article)
            .join(article.member, member)
            .fetchJoin()
            .join(article.category, category)
            .fetchJoin()
            .where(article.member.eq(memberParam))
            .orderBy(article.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long count =
        jpaQueryFactory
            .select(article.count())
            .from(article)
            .where(article.member.eq(memberParam))
            .fetchOne();

    List<ArticleResponse> responses =
        articles.stream().map(a -> new ArticleResponse(a, List.of())).toList();

    return new PageImpl<>(responses, pageable, count != null ? count : 0L);
  }

  @Override
  public Page<ArticleResponse> searchMineByTitle(
      Member memberParam, String keyword, Pageable pageable) {
    QArticle article = QArticle.article;
    QMember member = QMember.member;
    QCategory category = QCategory.category;

    List<Article> articles =
        jpaQueryFactory
            .selectFrom(article)
            .join(article.member, member)
            .fetchJoin()
            .join(article.category, category)
            .fetchJoin()
            .where(article.member.eq(memberParam).and(article.title.lower().like("%" + keyword.toLowerCase() + "%")))
            .orderBy(article.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long count =
        jpaQueryFactory
            .select(article.count())
            .from(article)
            .where(article.member.eq(memberParam).and(article.title.lower().like("%" + keyword.toLowerCase() + "%")))
            .fetchOne();

    List<ArticleResponse> responses =
        articles.stream().map(a -> new ArticleResponse(a, List.of())).toList();

    return new PageImpl<>(responses, pageable, count != null ? count : 0L);
  }

  @Override
  public Page<ArticleResponse> searchMineByTagName(
      Member memberParam, String tagName, Pageable pageable) {
    QArticle article = QArticle.article;
    QMember member = QMember.member;
    QCategory category = QCategory.category;
    QArticleTag articleTag = QArticleTag.articleTag;
    QTag tag = QTag.tag;

    List<Long> articleIds =
        jpaQueryFactory
            .select(articleTag.article.id)
            .from(articleTag)
            .join(articleTag.tag, tag)
            .where(tag.tagName.eq(tagName).and(articleTag.article.member.eq(memberParam)))
            .fetch();

    List<Article> articles =
        jpaQueryFactory
            .selectFrom(article)
            .join(article.member, member)
            .fetchJoin()
            .join(article.category, category)
            .fetchJoin()
            .where(article.id.in(articleIds))
            .orderBy(article.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long count = (long) articleIds.size();

    List<ArticleResponse> responses =
        articles.stream().map(a -> new ArticleResponse(a, List.of())).toList();

    return new PageImpl<>(responses, pageable, count);
  }

  @Override
  public Page<ArticleResponse> searchAllByTitle(String keyword, Pageable pageable) {
    QArticle article = QArticle.article;
    QMember member = QMember.member;
    QCategory category = QCategory.category;

    List<Article> articles =
        jpaQueryFactory
            .selectFrom(article)
            .join(article.member, member)
            .fetchJoin()
            .join(article.category, category)
            .fetchJoin()
            .where(article.title.lower().like("%" + keyword.toLowerCase() + "%"))
            .orderBy(article.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long count =
        jpaQueryFactory
            .select(article.count())
            .from(article)
            .where(article.title.lower().like("%" + keyword.toLowerCase() + "%"))
            .fetchOne();

    List<ArticleResponse> responses =
        articles.stream().map(a -> new ArticleResponse(a, List.of())).toList();

    return new PageImpl<>(responses, pageable, count != null ? count : 0L);
  }

  @Override
  public Page<ArticleResponse> searchAllByTagName(String tagName, Pageable pageable) {
    QArticle article = QArticle.article;
    QMember member = QMember.member;
    QCategory category = QCategory.category;
    QArticleTag articleTag = QArticleTag.articleTag;
    QTag tag = QTag.tag;

    List<Long> articleIds =
        jpaQueryFactory
            .select(articleTag.article.id)
            .from(articleTag)
            .join(articleTag.tag, tag)
            .where(tag.tagName.eq(tagName))
            .fetch();

    List<Article> articles =
        jpaQueryFactory
            .selectFrom(article)
            .join(article.member, member)
            .fetchJoin()
            .join(article.category, category)
            .fetchJoin()
            .where(article.id.in(articleIds))
            .orderBy(article.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long count = (long) articleIds.size();

    List<ArticleResponse> responses =
        articles.stream().map(a -> new ArticleResponse(a, List.of())).toList();

    return new PageImpl<>(responses, pageable, count);
  }

  @Override
  public Page<ArticleResponse> findAllByCategory(Long categoryId, Pageable pageable) {
    QArticle article = QArticle.article;
    QMember member = QMember.member;
    QCategory category = QCategory.category;

    List<Article> articles =
        jpaQueryFactory
            .selectFrom(article)
            .join(article.member, member)
            .fetchJoin()
            .join(article.category, category)
            .fetchJoin()
            .where(article.category.id.eq(categoryId))
            .orderBy(article.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long count =
        jpaQueryFactory
            .select(article.count())
            .from(article)
            .where(article.category.id.eq(categoryId))
            .fetchOne();

    List<ArticleResponse> responses =
        articles.stream().map(a -> new ArticleResponse(a, List.of())).toList();

    return new PageImpl<>(responses, pageable, count != null ? count : 0L);
  }

  @Override
  public Page<ArticleResponse> findMineByMemberAndCategory(
      Member memberParam, Long categoryId, Pageable pageable) {
    QArticle article = QArticle.article;
    QMember member = QMember.member;
    QCategory category = QCategory.category;

    List<Article> articles =
        jpaQueryFactory
            .selectFrom(article)
            .join(article.member, member)
            .fetchJoin()
            .join(article.category, category)
            .fetchJoin()
            .where(article.member.eq(memberParam).and(article.category.id.eq(categoryId)))
            .orderBy(article.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

    Long count =
        jpaQueryFactory
            .select(article.count())
            .from(article)
            .where(article.member.eq(memberParam).and(article.category.id.eq(categoryId)))
            .fetchOne();

    List<ArticleResponse> responses =
        articles.stream().map(a -> new ArticleResponse(a, List.of())).toList();

    return new PageImpl<>(responses, pageable, count != null ? count : 0L);
  }
}
