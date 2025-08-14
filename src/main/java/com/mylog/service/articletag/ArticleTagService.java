package com.mylog.service.articletag;

import com.mylog.model.entity.ArticleTag;
import com.mylog.repository.articletag.ArticleTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleTagService {
    private final ArticleTagRepository articleTagRepository;


    public void crateArticleTag(ArticleTag articleTag) {
        articleTagRepository.save(articleTag);
    }
}
