package com.mylog.entity;

import com.mylog.entity.compositekey.ArticleTagId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@IdClass(ArticleTagId.class)
public class ArticleTag {

    @Id
    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    @Id
    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;
}

