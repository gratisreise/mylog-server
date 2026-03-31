package com.mylog.domain.article.service;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.article.dto.request.ArticleCreateRequest;
import com.mylog.domain.article.dto.request.ArticleUpdateRequest;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.repository.ArticleRepository;
import com.mylog.domain.category.Category;
import com.mylog.domain.category.service.CategoryReader;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.MemberReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleWriter {

  private final ArticleRepository articleRepository;
  private final ArticleReader articleReader;
  private final MemberReader memberReader;
  private final CategoryReader categoryReader;
  private final AiService aiService;

  public Article create(ArticleCreateRequest request, Long memberId, String imageUrl) {
    Member member = memberReader.getById(memberId);
    Category category = categoryReader.getByMemberIdAndCategoryName(memberId, request.category());

    Article article = request.toEntity(member, category, imageUrl);
    Article savedArticle = articleRepository.save(article);

    // 비동기 AI 요약 + 태그 생성 트리거
    aiService.generateSummaryAsync(savedArticle.getId());

    return savedArticle;
  }

  public void update(ArticleUpdateRequest request, Long memberId, String imageUrl, Long articleId) {
    Article article = articleReader.getArticleById(articleId);

    if (!article.getMember().getId().equals(memberId)) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    Category category = categoryReader.getByMemberIdAndCategoryName(memberId, request.category());

    String finalImageUrl = imageUrl != null ? imageUrl : article.getArticleImg();
    article.update(request.title(), request.content(), finalImageUrl, category);

    // AI 요약 상태 리셋 후 비동기 재생성 트리거
    article.resetAiSummaryStatus();
    aiService.generateSummaryAsync(articleId);
  }

  public void delete(Long articleId, Long memberId) {
    Article article =
        articleRepository
            .findById(articleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ARTICLE_NOT_FOUND));

    if (!article.getMember().getId().equals(memberId)) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    articleRepository.delete(article);
  }
}
