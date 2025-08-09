package com.mylog.service.tag;


import com.mylog.model.entity.Article;
import com.mylog.model.entity.ArticleTag;
import com.mylog.model.entity.Tag;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.ArticleTagRepository;
import com.mylog.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;
    private final ArticleTagRepository articleTagRepository;

    @Transactional
    public void saveTag(List<String> tags, Article article){
        for(String tag : tags){
            //존재하는지 확인
            if(!tagRepository.existsByTagName(tag)){
                tagRepository.save(new Tag(tag));
            }

            Tag savedTag = tagRepository.findByTagName(tag)
                .orElseThrow(CMissingDataException::new);

            articleTagRepository.save(new ArticleTag(article, savedTag));
        }
    }

}
