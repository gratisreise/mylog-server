package com.mylog.api.tag;

import com.mylog.exception.CMissingDataException;
import com.mylog.domain.entity.Article;
import com.mylog.domain.entity.Tag;
import com.mylog.repository.tag.TagRepository;
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
