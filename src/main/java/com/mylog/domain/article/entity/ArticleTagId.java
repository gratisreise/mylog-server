<<<<<<<< HEAD:src/main/java/com/mylog/domain/article/entity/ArticleTagId.java
package com.mylog.domain.article.entity;
========
package com.mylog.article.entity;
>>>>>>>> origin/main:domain/src/main/java/com/mylog/article/entity/ArticleTagId.java

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ArticleTagId implements Serializable {
    private Long article;
    private Long tag;
}