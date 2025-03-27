package com.mylog.service.article;

import com.mylog.dto.ArticleCreateRequest;
import com.mylog.dto.ArticleResponse;
import com.mylog.dto.ArticleUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Article;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonArticleService{
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;

    //게시글 조회
    public ArticleResponse getArticle(Long id){
        Article article = articleRepository.findById(id)
            .orElseThrow(CMissingDataException::new);
        String category = article.getCategory().getCategoryName();
        String author = article.getMember().getNickname();
        return new ArticleResponse(article, category, author);
    };

    //전체 게시글 조회
    public Page<ArticleResponse> getArticles(Pageable pageable){
        return articleRepository.findAll(pageable)
            .map(ArticleResponse::from);
    };

    //전체 게시글 검색
    List<ArticleResponse> getArticles(String keyword, Pageable pageable){
        return null;
    };
}
