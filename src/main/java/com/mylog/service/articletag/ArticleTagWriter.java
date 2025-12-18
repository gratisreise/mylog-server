package com.mylog.service.articletag;

import com.mylog.domain.entity.Article;
import com.mylog.model.entity.ArticleTag;
import com.mylog.repository.articletag.ArticleTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleTagWriter {
    private final ArticleTagRepository articleTagRepository;


    public void crateArticleTag(ArticleTag articleTag) {
        articleTagRepository.save(articleTag);
    }

    @Async
    public void deleteArticleTag(Article article){
        articleTagRepository.deleteByArticle(article);
    }
}
