package com.mylog.article.repository;

import com.mylog.article.entity.Article;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleRepositoryCustom{

    @EntityGraph(attributePaths = {"member", "category"})
    Optional<Article> findById(Long articleId);
}
