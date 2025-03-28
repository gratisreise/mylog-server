package com.mylog.service.article;

import com.mylog.annotations.ServiceType;
import com.mylog.dto.ArticleCreateRequest;
import com.mylog.dto.ArticleDeleteRequest;
import com.mylog.dto.ArticleResponse;
import com.mylog.dto.ArticleUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Article;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import io.sentry.MeasurementUnit.Custom;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ServiceType(OauthProvider.SOCIAL)
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SocialArticleService implements ArticleService {
    private final ArticleRepository articleRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void createArticle(ArticleCreateRequest request, CustomUser customUser) {
        Category category = categoryRepository.findByCategoryName(request.getCategory())
            .orElseThrow(CMissingDataException::new);

        Member member = memberRepository
            .findById(Long.valueOf(customUser.getUsername()))
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
    @Transactional
    public void updateArticle(ArticleUpdateRequest request, CustomUser customUser) {
        Member requestMember = memberRepository.findByNickname(request.getAuthor())
            .orElseThrow(CMissingDataException::new);
        Member userMember = memberRepository.findById(Long.valueOf(customUser.getUsername()))
            .orElseThrow(CMissingDataException::new);

        if(!requestMember.getId().equals(userMember.getId())){
            throw new CUnAuthorizedException("허용 되지 않는 유저입니다.");
        }

        Article article = articleRepository.findById(request.getId())
            .orElseThrow(CMissingDataException::new);
        Category category = categoryRepository.findByCategoryName(request.getCategory())
            .orElseThrow(CMissingDataException::new);

        article.update(request, category);
    }

    @Override
    public void deleteArticle(ArticleDeleteRequest request, CustomUser customUser) {
        Member requestMember = memberRepository.findByNickname(request.getAuthor())
            .orElseThrow(CMissingDataException::new);
        Member userMember = memberRepository.findById(Long.valueOf(customUser.getUsername()))
            .orElseThrow(CMissingDataException::new);

        if(!requestMember.getId().equals(userMember.getId())){
            throw new CUnAuthorizedException("허용 되지 않는 유저입니다.");
        }

        articleRepository.deleteById(request.getId());
    }

    //내 게시글 목록 조회
    @Override
    public Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser) {
        return articleRepository
            .findAllByMemberId(Long.valueOf(customUser.getUsername()), pageable)
            .map(ArticleResponse::from);
    }

    //내 게시글 검색
    @Override
    public Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser, String keyword) {
        return articleRepository.findByMemberIdAndTitleContainingIgnoreCase(
                Long.valueOf(customUser.getUsername()), keyword, pageable)
            .map(ArticleResponse::from);
    }

}
