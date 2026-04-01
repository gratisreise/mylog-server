package com.mylog.domain.article.service;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.article.dto.request.ArticleQueryParam;
import com.mylog.domain.article.dto.response.ArticleResponse;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.repository.ArticleRepository;
import com.mylog.domain.article.repository.ArticleTagRepository;
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
  private final ArticleTagRepository articleTagRepository;

  public Page<ArticleResponse> getArticles(ArticleQueryParam params, Pageable pageable) {
    return articleRepository.searchArticles(params, pageable);
  }

  // 게시글 상세
  public ArticleResponse getArticle(Long id) {
    Article article =
        articleRepository
            .findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

    List<String> tags = getTagNames(id);
    return new ArticleResponse(article, tags);
  }

  // 태그 목록 조회
  private List<String> getTagNames(Long articleId) {
    return articleTagRepository.findAllByArticleId(articleId).stream()
        .map(at -> at.getTag().getTagName())
        .toList();
  }

  public boolean isExists(Long articleId) {
    return articleRepository.existsById(articleId);
  }

  public Article getArticleById(Long articleId) {
    return articleRepository
        .findById(articleId)
        .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));
  }
}
