package com.mylog.article.service;

import com.mylog.article.entity.Article;
import com.mylog.article.repository.ArticleTagRepository;
import com.mylog.article.entity.ArticleTag;
import com.mylog.tag.entity.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleTagWriter {
    private final ArticleTagRepository articleTagRepository;


    public void createArticleTag(Article article, List<Tag> tags){
        List<ArticleTag> articleTags = tags.stream()
            .map(tag -> new ArticleTag(article, tag))
            .toList();
        articleTagRepository.saveAll(articleTags);
    }
    public void crateArticleTag(ArticleTag articleTag) {
        articleTagRepository.save(articleTag);
    }

    @Async
    public void deleteArticleTag(Article article){
        articleTagRepository.deleteByArticle(article);
    }
}
