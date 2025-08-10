package com.mylog.repository.articletag;

import com.mylog.repository.member.MemberRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArticleTagRepositoryImpl implements ArticleTagRepositoryCustom {

    private final JPAQueryFactory queryFactory;
}
