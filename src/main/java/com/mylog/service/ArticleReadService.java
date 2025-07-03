package com.mylog.service;

import com.mylog.model.dto.article.ArticleResponse;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Article;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleReadService {
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;

    public Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser) {
        Long memberId = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new)
            .getId();

        return articleRepository.findAllByMemberId(memberId, pageable)
            .map(ArticleResponse::from);
    }

    public Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser,
        String keyword) {
        Long memberId = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new)
            .getId();
        return articleRepository
            .findByMemberIdAndTitleContainingIgnoreCase(memberId, keyword, pageable)
            .map(ArticleResponse::from);
    }


    public ArticleResponse getArticle(Long id){
        Article article = articleRepository.findById(id)
            .orElseThrow(CMissingDataException::new);
        String category = article.getCategory().getCategoryName();
        String author = article.getMember().getNickname();
        return new ArticleResponse(article, author, category);
    }

    public Page<ArticleResponse> getArticles(Pageable pageable){
        return articleRepository.findAll(pageable)
            .map(ArticleResponse::from);
    }

    public Page<ArticleResponse> getArticles(String keyword, String tag, Pageable pageable){
        return !isClear(keyword) ?
            articleRepository.findByTitleContainingIgnoreCase(keyword, pageable)
            .map(ArticleResponse::from) :
            articleRepository.findAllByTagName(tag, pageable)
                .map(ArticleResponse::from);
    }


    private boolean isClear(String s){
        return s == null || s.isEmpty();
    }

    public Article getArticleById(Long articleId) {
        return articleRepository.findById(articleId)
            .orElseThrow(CMissingDataException::new);
    }
}
