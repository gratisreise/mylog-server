package com.mylog.domain.article.entity;

import com.mylog.common.db.BaseEntity;
import com.mylog.common.enums.AnalyzeStatus;
import com.mylog.domain.category.Category;
import com.mylog.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Builder
@Getter
@AllArgsConstructor
public class Article extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
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

  @Column(columnDefinition = "TEXT")
  private String aiSummary;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private AnalyzeStatus aiSummaryStatus;

  public void update(String title, String content, String articleImg, Category category) {
    this.title = title;
    this.content = content;
    this.articleImg = articleImg;
    this.category = category;
  }

  public void updateAiSummary(String aiSummary) {
    this.aiSummary = aiSummary;
    this.aiSummaryStatus = AnalyzeStatus.COMPLETED;
  }

  public void markAiSummaryFailed() {
    this.aiSummaryStatus = AnalyzeStatus.FAILED;
  }

  public void resetAiSummaryStatus() {
    this.aiSummaryStatus = AnalyzeStatus.PENDING;
    this.aiSummary = null;
  }
}
