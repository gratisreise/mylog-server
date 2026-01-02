package com.mylog.article.repository;


import com.mylog.article.projections.ArticleProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleRepositoryCustom  {

    //전체 게시글 조회
    Page<ArticleProjection> findAllCustom(Pageable pageable);

    //내 게시글 목록조회
    Page<ArticleProjection> findMineByMember(Long memberId, Pageable pageable);

    //전체 게시글 검색
    Page<ArticleProjection> searchAll(String keyword, String tag, Pageable pageable);

    //내 게시글 검색


}
