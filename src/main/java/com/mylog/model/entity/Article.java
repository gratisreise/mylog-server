package com.mylog.model.entity;


import com.mylog.model.dto.article.ArticleCreateRequest;
import com.mylog.model.dto.article.ArticleUpdateRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Article {

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

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Article(ArticleCreateRequest request, Category category, Member member,
        String imageUrl) {
        this.title = request.title();
        this.content = request.content();
        this.category = category;
        this.member = member;
        this.articleImg = imageUrl;
    }

    public void update(ArticleUpdateRequest request, Category category) {
        this.title = request.title();
        this.content = request.content();
        this.category = category;
    }

    public void update(ArticleUpdateRequest request, Category category, String articleImg) {
        this.title = request.title();
        this.content = request.content();
        this.category = category;
        this.articleImg = articleImg;
    }

}
