package com.mylog.article.repository;

import com.mylog.article.entity.Article;
import com.mylog.api.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, ArticleRepositoryCustom{

    //게시글 제목검색
    Page<Article> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    //내 게시글 목록조회
    Page<Article> findAllByMember(Member memberId, Pageable pageable);

    //내 게시글 검색
    Page<Article> findByMemberAndTitleContainingIgnoreCase(
        Member member,
        String keyword,
        Pageable pageable
    );
}
