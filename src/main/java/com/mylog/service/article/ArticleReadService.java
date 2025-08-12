package com.mylog.service.article;

import com.mylog.model.dto.article.ArticleResponse;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Article;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.entity.Member;
import com.mylog.repository.article.ArticleRepository;
import com.mylog.repository.member.MemberRepository;
import com.mylog.service.member.MemberReadService;
import com.mylog.service.tag.TagReadService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ArticleReadService {
    private final ArticleRepository articleRepository;
    private final TagReadService tagReadService;
    private final MemberReadService memberReadService;
    private final MemberRepository memberRepository;

    public Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser) {
        Member member = memberReadService.getById(customUser.getMemberId());
        return articleRepository.findAllByMember(member, pageable)
            .map(this::createArticleResponse);
    }

    public Page<ArticleResponse> getArticles(Pageable pageable,
        CustomUser customUser, String keyword) {
        Member member = memberReadService.getById(customUser.getMemberId());
        return articleRepository
            .findByMemberAndTitleContainingIgnoreCase(member, keyword, pageable)
            .map(this::createArticleResponse);
    }


    public ArticleResponse getArticle(Long id){
        Article article = articleRepository.findById(id).orElseThrow(CMissingDataException::new);
        return createArticleResponse(article);
    }

    @Cacheable(value = "articles", key="#pageable.getPageNumber()")
    public Page<ArticleResponse> getArticles(Pageable pageable){
        return articleRepository.findAll(pageable)
            .map(this::createArticleResponse);
    }


    @Cacheable(value = "articles", key="'태그='+#tag")
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
        List<String> tags = tagReadService.getTags(article);
        return new ArticleResponse(article, tags);
    }

}
