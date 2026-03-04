package com.mylog.domain.article.service;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.article.dto.request.ArticleSearchRequest;
import com.mylog.domain.article.dto.response.ArticleResponse;
import com.mylog.domain.article.dto.response.ArticleTestResponse;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.repository.ArticleRepository;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.MemberReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleReader {
  private final ArticleRepository articleRepository;
  private final TagReader tagReader;
  private final MemberReader memberReader;

  // 전체 게시글 목록 조회
  public Page<ArticleResponse> getArticles(Pageable pageable) {
    return articleRepository.findAllCustom(pageable);
  }

  // 내 게시글 목록 조회
  public Page<ArticleResponse> getArticles(Pageable pageable, Long memberId) {
    Member member = memberReader.getById(memberId);
    return articleRepository.findMineByMember(member, pageable);
  }

  // 통합 검색
  public Page<ArticleResponse> search(ArticleSearchRequest request) {
    if (request.isMyArticles()) {
      Member member = memberReader.getById(request.memberId());
      return searchMine(member, request);
    }
    return searchAll(request);
  }

  // 전체 게시글 검색
  private Page<ArticleResponse> searchAll(ArticleSearchRequest request) {
    if (request.hasKeyword()) {
      return articleRepository.searchAllByTitle(request.keyword(), request.pageable());
    }
    if (request.hasTag()) {
      return articleRepository.searchAllByTagName(request.tag(), request.pageable());
    }
    if (request.hasCategory()) {
      return articleRepository.findAllByCategory(request.categoryId(), request.pageable());
    }
    return articleRepository.findAllCustom(request.pageable());
  }

  // 내 게시글 검색
  private Page<ArticleResponse> searchMine(Member member, ArticleSearchRequest request) {
    if (request.hasKeyword()) {
      return articleRepository.searchMineByTitle(member, request.keyword(), request.pageable());
    }
    if (request.hasTag()) {
      return articleRepository.searchMineByTagName(member, request.tag(), request.pageable());
    }
    if (request.hasCategory()) {
      return articleRepository.findMineByMemberAndCategory(
          member, request.categoryId(), request.pageable());
    }
    return articleRepository.findMineByMember(member, request.pageable());
  }

  // 게시글 상세
  public ArticleResponse getArticle(Long id) {
    Article article = articleRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));
    return new ArticleResponse(article, List.of());
  }

  public boolean isExists(Long articleId) {
    return articleRepository.existsById(articleId);
  }

  public List<ArticleTestResponse> getArticlesTest(Pageable pageable) {
    return articleRepository.findAll(pageable).getContent().stream()
        .map(ArticleTestResponse::from)
        .toList();
  }

  public Article getArticleById(Long articleId) {
    return articleRepository.findById(articleId).orElse(null);
  }
}
