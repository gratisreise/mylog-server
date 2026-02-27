package com.mylog.domain.tag.service;

import com.mylog.domain.article.entity.Article;
import com.mylog.domain.tag.entity.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagReader {
    private final TagRepository tagRepository;

    public List<String> getTags(Article article){
        return tagRepository.findByArticle(article);
    }

    public Tag getTagByTagName(String tagName){
        return tagRepository.findByTagName(tagName)
            .orElseThrow(CMissingDataException::new);
    }

}
