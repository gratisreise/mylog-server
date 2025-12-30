package com.mylog.article.repository;

import com.mylog.api.article.dto.ArticleResponse;
import com.mylog.api.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleRepositoryCustom  {

    //전체 게시글 조회
    Page<ArticleResponse> findAllCustom(Pageable pageable);

    //내 게시글 목록조회
    Page<ArticleResponse> findMineByMember(Member member, Pageable pageable);

    //내 게시글 제목검색
    Page<ArticleResponse> searchMineByTitle(Member member, String keyword, Pageable pageable);

    //전체 게시글 제목검색
    Page<ArticleResponse> searchAllByTitle(String keyword, Pageable pageable);

    //전체 게시글 태그검색
    Page<ArticleResponse> searchAllByTagName(String tagName, Pageable pageable);

}
