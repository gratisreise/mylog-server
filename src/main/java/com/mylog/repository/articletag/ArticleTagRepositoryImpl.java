package com.mylog.repository.articletag;

import com.mylog.model.entity.Article;
import com.mylog.model.entity.QArticleTag;
import com.mylog.model.entity.QTag;
import com.mylog.model.entity.Tag;
import com.mylog.repository.member.MemberRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArticleTagRepositoryImpl implements ArticleTagRepositoryCustom {

    private final JPAQueryFactory queryFactory;

}
