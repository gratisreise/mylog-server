<<<<<<<< HEAD:src/main/java/com/mylog/domain/comment/entity/Comment.java
<<<<<<<< HEAD:src/main/java/com/mylog/domain/comment/entity/Comment.java
package com.mylog.domain.comment.entity;

import com.mylog.domain.article.entity.Article;
import com.mylog.domain.member.Member;
import com.mylog.common.db.BaseEntity;
========
package com.mylog.comment.entity;

========
package com.mylog.comment.entity;

>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:domain/src/main/java/com/mylog/comment/entity/Comment.java
import com.mylog.BaseEntity;
import com.mylog.article.entity.Article;

import com.mylog.member.entity.Member;
<<<<<<<< HEAD:src/main/java/com/mylog/domain/comment/entity/Comment.java
>>>>>>>> origin/main:domain/src/main/java/com/mylog/comment/entity/Comment.java
========
>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:domain/src/main/java/com/mylog/comment/entity/Comment.java
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
public class Comment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private long parentId;

    @Column(length = 200)
    private String content;

    public void update(String content) {
        this.content = content;
    }

    public boolean isOwnedBy(Long customUserId) {
        return Objects.equals(customUserId, member.getId());
    }
}
