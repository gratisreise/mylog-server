package com.mylog.article.service;

import com.mylog.article.entity.Article;
import com.mylog.article.projections.ArticleProjection;
import com.mylog.article.repository.ArticleRepository;
import com.mylog.exception.CMissingDataException;
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
public class ArticleReader {
    private final ArticleRepository articleRepository;

    //게시글 단건조회
    public Article getById(Long articleId) {
        return articleRepository.findById(articleId)
            .orElseThrow(CMissingDataException::new);
    }

    //내 게시글 목록조회
    public Page<ArticleProjection> getArticles(Long memberId, Pageable pageable) {
        return articleRepository.findMineByMember(memberId, pageable);
    }


    //내 게시글 검색
    public Page<ArticleProjection> getArticles(String keyword, String tag, Pageable pageable, Long memberId) {
        return articleRepository.searchMine(keyword, tag, pageable, memberId);
    }

    // 전체 게시글 목록조회
    @Cacheable(value = "articles", key="#pageable.getPageNumber()")
    public Page<ArticleProjection> getArticles(Pageable pageable){
        return articleRepository.findAllCustom(pageable);
    }

    // 전체 게시글 검색
    @Cacheable(value = "articles", key="'태그='+#tag")
    public Page<ArticleProjection> getArticles(String keyword, String tag, Pageable pageable){
        return articleRepository.searchAll(keyword, tag, pageable);
    }

    //게시글 존재 확인
    public boolean isExists(Long articleId) {
        return articleRepository.existsById(articleId);
    }
}
