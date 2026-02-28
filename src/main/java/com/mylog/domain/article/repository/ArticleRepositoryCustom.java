package com.mylog.domain.article.repository;

import com.mylog.domain.article.dto.response.ArticleResponse;
import com.mylog.domain.member.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleRepositoryCustom {

    // 전체 게시글 조회
    Page<ArticleResponse> findAllCustom(Pageable pageable);

    // 내 게시글 목록 조회
    Page<ArticleResponse> findMineByMember(Member member, Pageable pageable);

    // 내 게시글 제목 검색
    Page<ArticleResponse> searchMineByTitle(Member member, String keyword, Pageable pageable);

    // 내 게시글 태그 검색
    Page<ArticleResponse> searchMineByTagName(Member member, String tagName, Pageable pageable);

    // 전체 게시글 제목 검색
    Page<ArticleResponse> searchAllByTitle(String keyword, Pageable pageable);

    // 전체 게시글 태그 검색
    Page<ArticleResponse> searchAllByTagName(String tagName, Pageable pageable);

    // 전체 게시글 카테고리 검색
    Page<ArticleResponse> findAllByCategory(Long categoryId, Pageable pageable);

    // 내 게시글 카테고리 검색
    Page<ArticleResponse> findMineByMemberAndCategory(Member member, Long categoryId, Pageable pageable);
}
