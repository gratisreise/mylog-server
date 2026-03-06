package com.mylog.domain.article.repository;

import com.mylog.domain.article.dto.ArticleResponse;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.member.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
    public Page<ArticleResponse> findAllCustom(Pageable pageable) {
        QArticle article = QArticle.article;
        QMember member = QMember.member;
        QCategory category = QCategory.category;

        List<Article> articles = queryFactory
            .selectFrom(article)
            .join(article.member, member).fetchJoin()
            .join(article.category, category).fetchJoin()
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<Long> articleIds = getArticleIds(articles);

        QArticleTag articleTag = QArticleTag.articleTag;
        QTag tag = QTag.tag;

        Map<Long, List<String>> articleTagMap = getMappedTags(articleTag, tag, articleIds);
        List<ArticleResponse> content = getContent(articles, articleTagMap);

        long total = queryFactory
            .select(article.count())
            .from(article)
            .fetchOne();

        return new PageImpl<>(content,pageable, total);
    }

    @Override
    public Page<ArticleResponse> findMineByMember(Member member, Pageable pageable) {
        QArticle article = QArticle.article;
        QMember qmember = QMember.member;
        QCategory category = QCategory.category;

        List<Article> articles = queryFactory
            .selectFrom(article)
            .join(article.member, qmember).fetchJoin()
            .join(article.category, category).fetchJoin()
            .where(article.member.eq(member))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<Long> articleIds = getArticleIds(articles);


        QArticleTag articleTag = QArticleTag.articleTag;
        QTag tag = QTag.tag;

        Map<Long, List<String>> articleTagMap = getMappedTags(articleTag, tag, articleIds);

        List<ArticleResponse> content = getContent(articles, articleTagMap);

        long total = queryFactory
            .select(article.count())
            .from(article)
            .where(article.member.eq(member))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ArticleResponse> searchMineByTitle(Member member, String keyword,
        Pageable pageable) {
        QArticle article = QArticle.article;
        QMember qmember = QMember.member;
        QCategory category = QCategory.category;

        List<Article> articles = queryFactory
            .selectFrom(article)
            .join(article.member, qmember).fetchJoin()
            .join(article.category, category).fetchJoin()
            .where(article.member.eq(member))
            .where(article.title.containsIgnoreCase(keyword))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<Long> articleIds = getArticleIds(articles);


        QArticleTag articleTag = QArticleTag.articleTag;
        QTag tag = QTag.tag;

        Map<Long, List<String>> articleTagMap = getMappedTags(articleTag, tag, articleIds);

        List<ArticleResponse> content = getContent(articles, articleTagMap);

        long total = queryFactory
            .select(article.count())
            .from(article)
            .where(article.member.eq(member))
            .where(article.title.containsIgnoreCase(keyword))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ArticleResponse> searchAllByTitle(String keyword, Pageable pageable) {
        QArticle article = QArticle.article;
        QMember qmember = QMember.member;
        QCategory category = QCategory.category;

        List<Article> articles = queryFactory
            .selectFrom(article)
            .join(article.member, qmember).fetchJoin()
            .join(article.category, category).fetchJoin()
            .where(article.title.containsIgnoreCase(keyword))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<Long> articleIds = getArticleIds(articles);


        QArticleTag articleTag = QArticleTag.articleTag;
        QTag tag = QTag.tag;

        Map<Long, List<String>> articleTagMap = getMappedTags(articleTag, tag, articleIds);

        List<ArticleResponse> content = getContent(articles, articleTagMap);

        long total = queryFactory
            .select(article.count())
            .from(article)
            .where(article.title.containsIgnoreCase(keyword))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<ArticleResponse> searchAllByTagName(String tagName, Pageable pageable) {
        QArticle article = QArticle.article;
        QArticleTag articleTag = QArticleTag.articleTag;
        QTag tag = QTag.tag;
        QMember member = QMember.member;
        QCategory category = QCategory.category;

        List<Article> articles = queryFactory
            .select(article).distinct()
            .from(articleTag)
            .join(articleTag.article, article)
            .join(articleTag.tag, tag)
            .join(article.member, member).fetchJoin()
            .join(article.category, category).fetchJoin()
            .where(tag.tagName.eq(tagName))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<Long> articleIds = getArticleIds(articles);

        Map<Long, List<String>> articleTagMap = getMappedTags(articleTag, tag, articleIds);

        List<ArticleResponse> content = getContent(articles, articleTagMap);

        long total = queryFactory
            .select(article.countDistinct())
            .from(articleTag)
            .join(articleTag.article, article)
            .join(articleTag.tag, tag)
            .where(tag.tagName.eq(tagName))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    private Map<Long, List<String>> getMappedTags(QArticleTag articleTag, QTag tag, List<Long> articleIds) {
        return queryFactory
            .select(articleTag.article.id, tag.tagName)
            .from(articleTag)
            .join(articleTag.tag, tag)
            .where(articleTag.article.id.in(articleIds))
            .fetch()
            .stream()
            .collect(Collectors.groupingBy(
                tuple -> tuple.get(articleTag.article.id),
                Collectors.mapping(
                    tuple -> tuple.get(tag.tagName),
                    Collectors.toList()
                )
            ));
    }


    private List<Long> getArticleIds(List<Article> articles) {
        return articles.stream()
            .map(Article::getId)
            .toList();
    }


    private List<ArticleResponse> getContent(List<Article> articles,
        Map<Long, List<String>> articleTagMap) {
        return articles.stream()
            .map(a -> new ArticleResponse(a,
                articleTagMap.getOrDefault(a.getId(), new ArrayList<>())))
            .collect(Collectors.toList());
    }

}
