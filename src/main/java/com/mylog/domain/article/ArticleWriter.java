package com.mylog.domain.article;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.article.dto.request.ArticleCreateRequest;
import com.mylog.domain.article.dto.request.ArticleUpdateRequest;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.repository.ArticleRepository;
import com.mylog.domain.category.Category;
import com.mylog.domain.category.service.CategoryReader;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.service.MemberReader;
import com.mylog.domain.tag.service.TagWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleWriter {

    private final ArticleRepository articleRepository;
    private final MemberReader memberReader;
    private final CategoryReader categoryReader;
    private final TagWriter tagWriter;

    public void create(ArticleCreateRequest request, Long memberId, String imageUrl) {
        Member member = memberReader.getById(memberId);
        Category category = categoryReader.getByMemberIdAndCategoryName(memberId, request.category());

        Article article = request.toEntity(member, category, imageUrl);
        articleRepository.save(article);

        if (request.tagNames() != null && !request.tagNames().isEmpty()) {
            tagWriter.saveTag(request.tagNames(), article);
        }
    }

    public void update(ArticleUpdateRequest request, Long memberId, String imageUrl, Long articleId) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!article.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Category category = categoryReader.getByMemberIdAndCategoryName(memberId, request.category());

        String finalImageUrl = imageUrl != null ? imageUrl : article.getArticleImg();
        article.update(request.title(), request.content(), finalImageUrl, category);
    }

    public void delete(Long articleId, Long memberId) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (!article.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        articleRepository.delete(article);
    }
}
