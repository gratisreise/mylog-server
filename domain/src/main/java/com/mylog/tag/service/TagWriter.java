package com.mylog.tag.service;


import com.mylog.article.entity.Article;
import com.mylog.article.entity.ArticleTag;
import com.mylog.article.service.ArticleTagWriter;
import com.mylog.api.tag.entity.Tag;
import com.mylog.tag.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TagWriter {
    private final TagRepository tagRepository;
    private final TagReader tagReader;
    private final ArticleTagWriter articleTagWriter;

    @Async
    public void saveTag(List<String> tags, Article article){
        for(String tag : tags){
            //존재하는지 확인
            if(!tagRepository.existsByTagName(tag)){
                tagRepository.save(new Tag(tag));
            }

            Tag savedTag = tagReader.getTagByTagName(tag);
            articleTagWriter.crateArticleTag(new ArticleTag(article, savedTag));
        }
    }

}
