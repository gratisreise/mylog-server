package com.mylog.domain.article.writer;

import com.mylog.domain.article.repository.ArticleTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleTagWriter {
    private final ArticleTagRepository articleTagRepository;

}
