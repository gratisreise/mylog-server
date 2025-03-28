package com.mylog.repository;

import com.mylog.entity.Article;
import java.nio.channels.FileChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    //내 게시글 목록조회
    Page<Article> findAllByMemberId(Long memberId, Pageable pageable);

    //제목검색
    Page<Article> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Article> findByMemberIdAndTitleContainingIgnoreCase(
        Long memberId,
        String keyword,
        Pageable pageable
    );

}
