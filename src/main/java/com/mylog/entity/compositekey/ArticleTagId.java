package com.mylog.entity.compositekey;

import com.mylog.entity.Article;
import com.mylog.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ArticleTagId implements Serializable {
    private Long article;
    private Long tag;
}