package com.mylog.tag.repository;


import com.mylog.tag.entity.Tag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, TagRepositoryCustom {
    boolean existsByTagName(String name);

    Optional<Tag> findByTagName(String tag);
}
