package com.mylog.service.article;

import com.mylog.dto.ArticleCreateRequest;
import com.mylog.dto.ArticleResponse;
import com.mylog.repository.ArticleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


public interface ArticleService {

    void createArticle(ArticleCreateRequest request);
    ArticleResponse getArticle(long id);
    List<ArticleResponse> getArticles();
    void updateArticle(ArticleUpdateRequest id);
    void deleteArticle(long id);
}
