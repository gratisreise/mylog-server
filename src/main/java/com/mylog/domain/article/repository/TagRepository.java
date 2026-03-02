package com.mylog.domain.article.repository;

import com.mylog.domain.article.entity.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, TagRepositoryCustom {
    boolean existsByTagName(String name);

    Optional<Tag> findByTagName(String tag);
}
