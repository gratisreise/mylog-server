<<<<<<<< HEAD:src/main/java/com/mylog/domain/tag/repository/TagRepository.java
<<<<<<<< HEAD:src/main/java/com/mylog/domain/tag/repository/TagRepository.java
package com.mylog.domain.tag.repository;

import com.mylog.domain.tag.entity.Tag;
import com.mylog.tag.entity.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, TagRepositoryCustom {
    boolean existsByTagName(String name);

    Optional<Tag> findByTagName(String tag);
}
