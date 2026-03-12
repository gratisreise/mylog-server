package com.mylog.domain.article.repository;

import com.mylog.domain.article.dto.request.ArticleQueryParam;
import com.mylog.domain.article.dto.response.ArticleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleRepositoryCustom {

  /**
   * 통합 게시글 검색/조회 메서드
   *
   * @param params 검색 파라미터 (memberId, keyword, tag, categoryId)
   * @param pageable 페이징 정보
   * @return 필터링된 게시글 목록
   */
  Page<ArticleResponse> searchArticles(ArticleQueryParam params, Pageable pageable);
}
