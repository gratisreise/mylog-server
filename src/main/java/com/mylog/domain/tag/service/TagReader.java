<<<<<<<< HEAD:src/main/java/com/mylog/domain/tag/service/TagReader.java
package com.mylog.domain.tag.service;

import com.mylog.domain.tag.entity.Tag;
import com.mylog.domain.tag.repository.TagRepository;
import com.mylog.common.exception.CMissingDataException;
import com.mylog.domain.article.entity.Article;
========
package com.mylog.tag.service;


import com.mylog.article.entity.Article;
import com.mylog.tag.entity.Tag;
import com.mylog.tag.repository.TagRepository;
>>>>>>>> origin/main:domain/src/main/java/com/mylog/tag/service/TagReader.java
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.mylog.exception.common.CMissingDataException;
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
