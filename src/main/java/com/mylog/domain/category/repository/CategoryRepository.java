package com.mylog.domain.category.repository;

import com.mylog.domain.category.Category;
import com.mylog.domain.member.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, CategoryRepositoryCustom {

    Optional<Category> findByMemberIdAndCategoryName(Long memberId, String category);

    boolean existsByMemberAndCategoryName(Member member, String categoryName);

    @Query("SELECT c FROM Category c JOIN FETCH c.member")
    List<Category> findByMember(Member member);

    int countByMember(Member member);
}
