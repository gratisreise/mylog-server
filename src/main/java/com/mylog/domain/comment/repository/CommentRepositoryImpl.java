<<<<<<<< HEAD:src/main/java/com/mylog/domain/comment/repository/CommentRepositoryImpl.java
<<<<<<<< HEAD:src/main/java/com/mylog/domain/comment/repository/CommentRepositoryImpl.java
package com.mylog.domain.comment.repository;

import com.mylog.domain.comment.entity.Comment;
import com.mylog.domain.member.Member;
import com.mylog.model.entity.QArticle;
import com.mylog.model.entity.QComment;
import com.mylog.model.entity.QMember;
========
package com.mylog.comment.repository;

========
package com.mylog.comment.repository;

>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:domain/src/main/java/com/mylog/comment/repository/CommentRepositoryImpl.java
import com.mylog.article.entity.QArticle;
import com.mylog.comment.entity.Comment;

import com.mylog.comment.entity.QComment;
import com.mylog.comment.repository.CommentRepositoryCustom;
import com.mylog.member.entity.Member;
import com.mylog.member.entity.QMember;
<<<<<<<< HEAD:src/main/java/com/mylog/domain/comment/repository/CommentRepositoryImpl.java
>>>>>>>> origin/main:domain/src/main/java/com/mylog/comment/repository/CommentRepositoryImpl.java
========
>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:domain/src/main/java/com/mylog/comment/repository/CommentRepositoryImpl.java
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
    public Page<Comment> findMyArticlesCommentsByMemberId(Long memberId, Pageable pageable) {
        QArticle article = QArticle.article;
        QComment comment = QComment.comment;

        List<Comment> content = queryFactory
            .select(comment)
            .from(comment)
            .leftJoin(comment.article, article)
            .where(article.member.id.eq(memberId))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(comment.count())
            .from(comment)
            .join(comment.article, article)
            .where(article.member.id.eq(memberId))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
