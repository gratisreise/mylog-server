package com.mylog.domain.article.service;

import com.mylog.domain.article.dto.ArticleResponse;
import com.mylog.domain.article.dto.ArticleTestResponse;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.repository.ArticleRepository;
import com.mylog.common.exception.CMissingDataException;
import com.mylog.common.security.CustomUser;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.service.MemberReader;
import com.mylog.domain.tag.service.TagReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ArticleReader {
    private final ArticleRepository articleRepository;
    private final TagReader tagReader;
    private final MemberReader memberReader;

    //내 게시글 목록조회
    public Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser) {
        Member member = memberReader.getById(customUser.getMemberId());
        return articleRepository.findMineByMember(member, pageable);
    }

    //내 게시글 검색
    public Page<ArticleResponse> getArticles(Pageable pageable,
        CustomUser customUser, String keyword) {
        log.info("userId:{}", customUser.getMemberId());
        Member member = memberReader.getById(customUser.getMemberId());
        return articleRepository.searchMineByTitle(member, keyword, pageable);
    }

    //게시글 상세
    public ArticleResponse getArticle(Long id){
        Article article = articleRepository.findById(id).orElseThrow(CMissingDataException::new);
        List<String> tags = tagReader.getTags(article);
        return new ArticleResponse(article, tags);
    }

    public boolean isExists(Long articleId){
        return articleRepository.existsById(articleId);
    }

    public List<ArticleTestResponse> getArticles(Pageable pageable){
        return articleRepository.findAll(pageable)
            .getContent()
            .stream().map(ArticleTestResponse::from)
            .toList();
    }


    // 전체 게시글 검색
    public Page<ArticleResponse> getArticles(String keyword, String tag, Pageable pageable){
        StringBuilder sb = new StringBuilder();
        return !isClear(keyword) ?
            articleRepository.searchAllByTitle(keyword, pageable) :
            articleRepository.searchAllByTagName(tag, pageable);
    }

    public Article getArticleById(Long articleId) {
        return articleRepository.findById(articleId).orElseThrow(CMissingDataException::new);
    }

    private boolean isClear(String s){
        return s == null || s.isEmpty();
    }

}
