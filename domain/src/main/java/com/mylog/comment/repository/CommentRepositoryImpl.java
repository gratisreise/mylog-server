package com.mylog.comment.repository;

import com.mylog.article.entity.QArticle;
import com.mylog.comment.entity.Comment;

import com.mylog.comment.entity.QComment;
import com.mylog.comment.repository.CommentRepositoryCustom;
import com.mylog.member.entity.Member;
import com.mylog.member.entity.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Comment> findMyArticlesComments(Member member, Pageable pageable) {
        QArticle article = QArticle.article;
        QMember members = QMember.member;
        QComment comment = QComment.comment;

        List<Comment> content = queryFactory
            .selectFrom(comment)
            .join(comment.article, article)
            .join(comment.member, members).fetchJoin()
            .where(article.member.eq(member))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(comment.count())
            .from(comment)
            .from(comment)
            .join(comment.article, article)
            .where(article.member.eq(member))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
