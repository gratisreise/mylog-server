package com.mylog.domain.article.entity;

import com.mylog.domain.article.entity.Tag;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@NoArgsConstructor
@IdClass(ArticleTagId.class)
@AllArgsConstructor
@Getter
@Builder
public class ArticleTag {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Article article;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleTag that = (ArticleTag) o;
        return Objects.equals(article != null ? article.getId() : null, that.article != null ? that.article.getId() : null)
                && Objects.equals(tag != null ? tag.getId() : null, that.tag != null ? that.tag.getId() : null);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                article != null ? article.getId() : null,
                tag != null ? tag.getId() : null
        );
    }
}
