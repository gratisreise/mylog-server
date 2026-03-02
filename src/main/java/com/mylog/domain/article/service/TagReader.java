package com.mylog.domain.article.service;


import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.entity.Tag;
import com.mylog.domain.article.repository.TagRepository;
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
            .orElseThrow(() -> BusinessException(ErrorCode.TAG));
    }

}
