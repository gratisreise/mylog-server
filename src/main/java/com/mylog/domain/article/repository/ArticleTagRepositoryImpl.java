package com.mylog.domain.article.repository.impl;


import com.mylog.domain.article.repository.ArticleTagRepositoryCustom;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArticleTagRepositoryImpl implements ArticleTagRepositoryCustom {

    private final JPAQueryFactory queryFactory;

}
