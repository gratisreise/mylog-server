<<<<<<<< HEAD:src/main/java/com/mylog/domain/category/repository/CategoryRepository.java
<<<<<<<< HEAD:src/main/java/com/mylog/domain/category/repository/CategoryRepository.java
package com.mylog.domain.category.repository;

import com.mylog.domain.category.Category;
import com.mylog.domain.member.Member;
========
package com.mylog.category.repository;

import com.mylog.category.entity.Category;
import com.mylog.member.entity.Member;
>>>>>>>> origin/main:domain/src/main/java/com/mylog/category/repository/CategoryRepository.java
========
package com.mylog.category.repository;

import com.mylog.category.entity.Category;
import com.mylog.member.entity.Member;
>>>>>>>> df0a55de6d27f9fdc5dd1d7257f9e30801976b60:domain/src/main/java/com/mylog/category/repository/CategoryRepository.java
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipFile;
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

    List<Category> findByMemberId(long memberId);
}
