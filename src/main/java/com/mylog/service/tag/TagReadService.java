package com.mylog.service.tag;

import com.mylog.model.entity.Article;
import com.mylog.model.entity.Tag;
import com.mylog.repository.tag.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagReadService {
    private final TagRepository tagRepository;

    public List<String> getTags(Article article){
        return tagRepository.findByArticle(article);
    }

}
