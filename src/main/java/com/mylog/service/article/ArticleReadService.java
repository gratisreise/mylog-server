package com.mylog.service.article;

import com.mylog.model.dto.article.ArticleResponse;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Article;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.MemberRepository;
import com.mylog.service.articletage.ArticleTagReadService;
import java.util.List;
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
    private final ArticleTagReadService articleTagReadService;
    private final MemberRepository memberRepository;

    public Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser) {
        Long memberId = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new)
            .getId();

        return articleRepository.findAllByMemberId(memberId, pageable)
            .map(this::createArticleResponse);
    }

    public Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser,
        String keyword) {
        Long memberId = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new)
            .getId();
        return articleRepository
            .findByMemberIdAndTitleContainingIgnoreCase(memberId, keyword, pageable)
            .map(this::createArticleResponse);
    }


    public ArticleResponse getArticle(Long id){
        Article article = articleRepository.findById(id).orElseThrow(CMissingDataException::new);
        return createArticleResponse(article);
    }

    public Page<ArticleResponse> getArticles(Pageable pageable){
        return articleRepository.findAll(pageable)
            .map(this::createArticleResponse);
    }

    public Page<ArticleResponse> getArticles(String keyword, String tag, Pageable pageable){
        return !isClear(keyword) ?
            articleRepository.findByTitleContainingIgnoreCase(keyword, pageable)
            .map(this::createArticleResponse) :
            articleRepository.findAllByTagName(tag, pageable)
                .map(this::createArticleResponse);
    }


    public Article getArticleById(Long articleId) {
        return articleRepository.findById(articleId)
            .orElseThrow(CMissingDataException::new);
    }

    private boolean isClear(String s){
        return s == null || s.isEmpty();
    }

    private ArticleResponse createArticleResponse(Article article){
        List<String> tags = articleTagReadService.getTags(article);
        return new ArticleResponse(article, tags);
    }

}
