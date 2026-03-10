package com.mylog.domain.article.service;

import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.repository.TagRepository;
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

  @Async("threadPoolTaskExecutor")
  public void saveTag(List<String> tags, Article article) {}
}
