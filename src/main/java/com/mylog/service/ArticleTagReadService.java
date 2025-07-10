package com.mylog.service;

import com.mylog.model.entity.Article;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.ArticleTagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleTagReadService {
    private final ArticleTagRepository articleTagRepository;


    public List<String> getTags(Article article){
        return articleTagRepository.findByArticle(article)
            .stream().map(data -> data.getTag().getTagName())
            .toList();
    }

}
