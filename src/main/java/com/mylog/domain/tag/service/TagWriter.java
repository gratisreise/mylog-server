<<<<<<<< HEAD:src/main/java/com/mylog/domain/tag/service/TagWriter.java
package com.mylog.domain.tag.service;


import com.mylog.article.entity.Article;
import com.mylog.article.entity.ArticleTag;
import com.mylog.article.service.ArticleTagWriter;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.entity.ArticleTag;
import com.mylog.domain.article.service.ArticleTagWriter;
import com.mylog.domain.tag.entity.Tag;
import com.mylog.domain.tag.repository.TagRepository;
import com.mylog.tag.entity.Tag;
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
            ArticleTag articleTag = ArticleTag.builder()
                .article(article)
                .tag(savedTag)
                .build();
            articleTagWriter.crateArticleTag(new ArticleTag(article, savedTag));
        }
    }

    public List<Tag> getTagsOrCreate(List<String> tagNames) {
        List<Tag> tags = tagNames.stream()
            .filter(name -> !tagRepository.existsByTagName(name))
            .map(Tag::new).toList();
        return tagRepository.saveAll(tags);
    }

}
