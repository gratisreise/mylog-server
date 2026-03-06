package com.mylog.article.projections;

import com.mylog.category.entity.Category;
import com.mylog.member.entity.Member;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ArticleProjection {
    private Long id;
    private String title;
    private String memberName;
    private String categoryName;
    private String content;
    private String articleImg;
    private String tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
