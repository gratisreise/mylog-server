package com.mylog.domain.article.repository.impl;

import com.mylog.domain.article.dto.response.ArticleResponse;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.repository.ArticleRepositoryCustom;
import com.mylog.domain.member.Member;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<ArticleResponse> findAllCustom(Pageable pageable) {
        String jpql = "SELECT a FROM Article a JOIN FETCH a.member JOIN FETCH a.category ORDER BY a.createdAt DESC";
        List<Article> articles = em.createQuery(jpql, Article.class)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

        String countJpql = "SELECT COUNT(a) FROM Article a";
        Long count = em.createQuery(countJpql, Long.class).getSingleResult();

        List<ArticleResponse> responses = articles.stream()
            .map(a -> new ArticleResponse(a, List.of()))
            .toList();

        return new PageImpl<>(responses, pageable, count);
    }

    @Override
    public Page<ArticleResponse> findMineByMember(Member member, Pageable pageable) {
        String jpql = "SELECT a FROM Article a JOIN FETCH a.member JOIN FETCH a.category WHERE a.member = :member ORDER BY a.createdAt DESC";
        List<Article> articles = em.createQuery(jpql, Article.class)
            .setParameter("member", member)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

        String countJpql = "SELECT COUNT(a) FROM Article a WHERE a.member = :member";
        Long count = em.createQuery(countJpql, Long.class)
            .setParameter("member", member)
            .getSingleResult();

        List<ArticleResponse> responses = articles.stream()
            .map(a -> new ArticleResponse(a, List.of()))
            .toList();

        return new PageImpl<>(responses, pageable, count);
    }

    @Override
    public Page<ArticleResponse> searchMineByTitle(Member member, String keyword, Pageable pageable) {
        String jpql = "SELECT a FROM Article a JOIN FETCH a.member JOIN FETCH a.category WHERE a.member = :member AND LOWER(a.title) LIKE LOWER(:keyword) ORDER BY a.createdAt DESC";
        List<Article> articles = em.createQuery(jpql, Article.class)
            .setParameter("member", member)
            .setParameter("keyword", "%" + keyword + "%")
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

        String countJpql = "SELECT COUNT(a) FROM Article a WHERE a.member = :member AND LOWER(a.title) LIKE LOWER(:keyword)";
        Long count = em.createQuery(countJpql, Long.class)
            .setParameter("member", member)
            .setParameter("keyword", "%" + keyword + "%")
            .getSingleResult();

        List<ArticleResponse> responses = articles.stream()
            .map(a -> new ArticleResponse(a, List.of()))
            .toList();

        return new PageImpl<>(responses, pageable, count);
    }

    @Override
    public Page<ArticleResponse> searchMineByTagName(Member member, String tagName, Pageable pageable) {
        String jpql = "SELECT a FROM Article a JOIN FETCH a.member JOIN FETCH a.category " +
            "WHERE a.member = :member AND a.id IN " +
            "(SELECT at.article.id FROM ArticleTag at JOIN at.tag t WHERE t.tagName = :tagName) " +
            "ORDER BY a.createdAt DESC";
        List<Article> articles = em.createQuery(jpql, Article.class)
            .setParameter("member", member)
            .setParameter("tagName", tagName)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

        String countJpql = "SELECT COUNT(at) FROM ArticleTag at JOIN at.tag t JOIN at.article a " +
            "WHERE t.tagName = :tagName AND a.member = :member";
        Long count = em.createQuery(countJpql, Long.class)
            .setParameter("member", member)
            .setParameter("tagName", tagName)
            .getSingleResult();

        List<ArticleResponse> responses = articles.stream()
            .map(a -> new ArticleResponse(a, List.of()))
            .toList();

        return new PageImpl<>(responses, pageable, count);
    }

    @Override
    public Page<ArticleResponse> searchAllByTitle(String keyword, Pageable pageable) {
        String jpql = "SELECT a FROM Article a JOIN FETCH a.member JOIN FETCH a.category WHERE LOWER(a.title) LIKE LOWER(:keyword) ORDER BY a.createdAt DESC";
        List<Article> articles = em.createQuery(jpql, Article.class)
            .setParameter("keyword", "%" + keyword + "%")
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

        String countJpql = "SELECT COUNT(a) FROM Article a WHERE LOWER(a.title) LIKE LOWER(:keyword)";
        Long count = em.createQuery(countJpql, Long.class)
            .setParameter("keyword", "%" + keyword + "%")
            .getSingleResult();

        List<ArticleResponse> responses = articles.stream()
            .map(a -> new ArticleResponse(a, List.of()))
            .toList();

        return new PageImpl<>(responses, pageable, count);
    }

    @Override
    public Page<ArticleResponse> searchAllByTagName(String tagName, Pageable pageable) {
        String jpql = "SELECT a FROM Article a JOIN FETCH a.member JOIN FETCH a.category " +
            "WHERE a.id IN (SELECT at.article.id FROM ArticleTag at JOIN at.tag t WHERE t.tagName = :tagName) " +
            "ORDER BY a.createdAt DESC";
        List<Article> articles = em.createQuery(jpql, Article.class)
            .setParameter("tagName", tagName)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

        String countJpql = "SELECT COUNT(at) FROM ArticleTag at JOIN at.tag t WHERE t.tagName = :tagName";
        Long count = em.createQuery(countJpql, Long.class)
            .setParameter("tagName", tagName)
            .getSingleResult();

        List<ArticleResponse> responses = articles.stream()
            .map(a -> new ArticleResponse(a, List.of()))
            .toList();

        return new PageImpl<>(responses, pageable, count);
    }

    @Override
    public Page<ArticleResponse> findAllByCategory(Long categoryId, Pageable pageable) {
        String jpql = "SELECT a FROM Article a JOIN FETCH a.member JOIN FETCH a.category WHERE a.category.id = :categoryId ORDER BY a.createdAt DESC";
        List<Article> articles = em.createQuery(jpql, Article.class)
            .setParameter("categoryId", categoryId)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

        String countJpql = "SELECT COUNT(a) FROM Article a WHERE a.category.id = :categoryId";
        Long count = em.createQuery(countJpql, Long.class)
            .setParameter("categoryId", categoryId)
            .getSingleResult();

        List<ArticleResponse> responses = articles.stream()
            .map(a -> new ArticleResponse(a, List.of()))
            .toList();

        return new PageImpl<>(responses, pageable, count);
    }

    @Override
    public Page<ArticleResponse> findMineByMemberAndCategory(Member member, Long categoryId, Pageable pageable) {
        String jpql = "SELECT a FROM Article a JOIN FETCH a.member JOIN FETCH a.category WHERE a.member = :member AND a.category.id = :categoryId ORDER BY a.createdAt DESC";
        List<Article> articles = em.createQuery(jpql, Article.class)
            .setParameter("member", member)
            .setParameter("categoryId", categoryId)
            .setFirstResult((int) pageable.getOffset())
            .setMaxResults(pageable.getPageSize())
            .getResultList();

        String countJpql = "SELECT COUNT(a) FROM Article a WHERE a.member = :member AND a.category.id = :categoryId";
        Long count = em.createQuery(countJpql, Long.class)
            .setParameter("member", member)
            .setParameter("categoryId", categoryId)
            .getSingleResult();

        List<ArticleResponse> responses = articles.stream()
            .map(a -> new ArticleResponse(a, List.of()))
            .toList();

        return new PageImpl<>(responses, pageable, count);
    }
}
