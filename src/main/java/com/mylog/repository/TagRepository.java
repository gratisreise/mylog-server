package com.mylog.repository;

import com.mylog.model.entity.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByTagName(String name);

    Optional<Tag> findByTagName(String tag);
}
