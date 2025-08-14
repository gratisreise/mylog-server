package com.mylog.service.tag;


import com.mylog.model.entity.Article;
import com.mylog.model.entity.ArticleTag;
import com.mylog.model.entity.Tag;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.articletag.ArticleTagRepository;
import com.mylog.repository.tag.TagRepository;
import com.mylog.service.articletag.ArticleTagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {
    private final TagRepository tagRepository;
    private final TagReader tagReader;
    private final ArticleTagService articleTagService;

    public void saveTag(List<String> tags, Article article){
        for(String tag : tags){
            //존재하는지 확인
            if(!tagRepository.existsByTagName(tag)){
                tagRepository.save(new Tag(tag));
            }

            Tag savedTag = tagReader.getTagByTagName(tag);
            articleTagService.crateArticleTag(new ArticleTag(article, savedTag));
        }
    }

}
