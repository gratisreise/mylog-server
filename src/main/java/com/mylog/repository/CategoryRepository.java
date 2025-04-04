package com.mylog.repository;

import com.mylog.entity.Category;
import com.mylog.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByCategoryName(String category);

    List<Category> findByMember(Member member);
}
