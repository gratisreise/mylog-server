package com.mylog.tag.service;


import com.mylog.article.entity.Article;
import com.mylog.tag.entity.Tag;
import com.mylog.tag.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.mylog.exception.CMissingDataException;
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
