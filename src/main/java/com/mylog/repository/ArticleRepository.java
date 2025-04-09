package com.mylog.repository;

import com.mylog.entity.Article;
import java.nio.channels.FileChannel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    //게시글 제목검색
    Page<Article> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    //내 게시글 목록조회
    Page<Article> findAllByMemberId(Long memberId, Pageable pageable);

    //내 게시글 검색
    Page<Article> findByMemberIdAndTitleContainingIgnoreCase(
        Long memberId,
        String keyword,
        Pageable pageable
    );


    //태그검색
    @Query("""
    SELECT a FROM Article a
    JOIN ArticleTag at ON a.id = at.article.id
    JOIN Tag t ON at.tag.id = t.id
    WHERE t.tagName = :tagName
    """)
    Page<Article> findAllByTagName(String tagName, Pageable pageable);



}
