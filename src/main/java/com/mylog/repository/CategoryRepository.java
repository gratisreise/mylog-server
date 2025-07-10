package com.mylog.repository;

import com.mylog.model.entity.Category;
import com.mylog.model.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByMemberAndCategoryName(Member member, String category);

    List<Category> findByMember(Member member);
}
