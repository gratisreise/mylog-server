package com.mylog.domain.article.entity;


import com.mylog.domain.article.dto.ArticleUpdateRequest;
import com.mylog.common.db.BaseEntity;
import com.mylog.domain.category.Category;
import com.mylog.domain.member.Member;
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
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Builder
@Getter
@Setter
@AllArgsConstructor
public class Article extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 30, nullable = false)
    private String title;

    @Column(length = 3000, nullable = false)
    private String content;

    @Column(length = 300)
    private String articleImg;

    public boolean isOwnedBy(Long userId){
        return Objects.equals(this.member.getId(), userId);
    }

    public void update(ArticleUpdateRequest request, Category category, String articleImg) {
        this.title = request.title();
        this.content = request.content();
        this.category = category;
        this.articleImg = articleImg;
    }

}
