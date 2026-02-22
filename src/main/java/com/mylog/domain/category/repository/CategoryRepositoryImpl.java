<<<<<<<< HEAD:src/main/java/com/mylog/domain/category/repository/CategoryRepositoryImpl.java
package com.mylog.domain.category.repository;
========
package com.mylog.category.repository;
>>>>>>>> origin/main:domain/src/main/java/com/mylog/category/repository/CategoryRepositoryImpl.java

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepositoryCustom {

    private final JPAQueryFactory queryFactory;
}
