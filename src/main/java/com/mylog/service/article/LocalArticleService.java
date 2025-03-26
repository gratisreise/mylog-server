package com.mylog.service.article;

import com.mylog.annotations.ServiceType;
import com.mylog.dto.ArticleCreateRequest;
import com.mylog.dto.ArticleResponse;
import com.mylog.dto.ArticleUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Article;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import com.mylog.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ServiceType(OauthProvider.LOCAL)
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalArticleService implements ArticleService{

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public void createArticle(ArticleCreateRequest request, CustomUser customUser) {

        Category category = categoryRepository.findByCategoryName(request.getCategory())
            .orElseThrow(CMissingDataException::new);
        Member member = memberRepository.findByEmail(customUser.getUsername())
            .orElseThrow(CMissingDataException::new);

        Article article = Article.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .category(category)
            .member(member)
            .build();

        articleRepository.save(article);
    }

    @Override
    public ArticleResponse getArticle(Long id) {
        return null;
    }

    @Override
    @Transactional
    public void updateArticle(ArticleUpdateRequest request, CustomUser customUser) {

    }

    @Override
    @Transactional
    public void deleteArticle(Long id, CustomUser customUser) {

    }

    @Override
    public List<ArticleResponse> getArticles() {
        return List.of();
    }

    @Override
    public List<ArticleResponse> getArticles(CustomUser customUser) {
        return List.of();
    }

    @Override
    public List<ArticleResponse> getArticles(CustomUser customUser, String keyword) {
        return List.of();
    }

    @Override
    public List<ArticleResponse> getArticles(String keyword) {
        return List.of();
    }
}
