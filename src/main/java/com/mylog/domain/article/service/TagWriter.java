package com.mylog.domain.article.service;

import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.entity.ArticleTag;
import com.mylog.domain.article.entity.Tag;
import com.mylog.domain.article.repository.ArticleTagRepository;
import com.mylog.domain.article.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TagWriter {
  private final TagRepository tagRepository;
  private final ArticleTagRepository articleTagRepository;

  //AI가 생성한 태그를 저장
  public void saveAiTags(List<String> tagNames, Article article) {
    // 기존 연결된 태그 삭제
    articleTagRepository.deleteByArticle(article);

    for (String tagName : tagNames) {
      if (tagName == null || tagName.isBlank()) continue;

      // 10자 초과 시 자름
      String trimmedName = tagName.length() > 10 ? tagName.substring(0, 10) : tagName;

      // 기존 태그 조회 or 새로 생성
      Tag tag =
          tagRepository
              .findByTagName(trimmedName)
              .orElseGet(() -> tagRepository.save(Tag.from(trimmedName)));

      // ArticleTag 연결 생성
      ArticleTag articleTag = ArticleTag.builder().article(article).tag(tag).build();
      articleTagRepository.save(articleTag);
    }
  }
}
