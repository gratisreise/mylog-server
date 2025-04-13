package com.mylog.service.article;

import com.mylog.annotations.ServiceType;
import com.mylog.dto.article.ArticleCreateRequest;
import com.mylog.dto.article.ArticleDeleteRequest;
import com.mylog.dto.article.ArticleResponse;
import com.mylog.dto.article.ArticleUpdateRequest;
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
import com.mylog.service.TagService;
import com.mylog.service.notification.CommonNotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ServiceType(OauthProvider.LOCAL)
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LocalArticleService implements ArticleService{

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final TagService tagService;

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

        Article savedArticle = articleRepository.save(article);

        tagService.saveTag(request.getTags(), savedArticle);
    }


    @Override
    @Transactional
    public void updateArticle(ArticleUpdateRequest request, CustomUser customUser) {
        Member requestMember = memberRepository.findByNickname(request.getAuthor())
            .orElseThrow(CMissingDataException::new);
        Member userMember = memberRepository.findByEmail(customUser.getUsername())
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
    @Transactional
    public void deleteArticle(ArticleDeleteRequest request, CustomUser customUser) {
        Member requestMember = memberRepository.findByNickname(request.getAuthor())
            .orElseThrow(CMissingDataException::new);
        Member userMember = memberRepository.findByEmail(customUser.getUsername())
            .orElseThrow(CMissingDataException::new);

        if(!requestMember.getId().equals(userMember.getId())){
            throw new CUnAuthorizedException("허용 되지 않는 유저입니다.");
        }

        articleRepository.deleteById(request.getId());
    }

    //내 게시글 목록조회
    @Override
    public Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser) {
        Long memberId = memberRepository.findByEmail(customUser.getUsername())
            .orElseThrow(CMissingDataException::new)
            .getId();

        return articleRepository.findAllByMemberId(memberId, pageable)
            .map(ArticleResponse::from);
    }

    //내 게시글 검색
    @Override
    public Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser,
        String keyword) {
        Long memberId = memberRepository.findByEmail(customUser.getUsername())
            .orElseThrow(CMissingDataException::new)
            .getId();
        return articleRepository
            .findByMemberIdAndTitleContainingIgnoreCase(memberId, keyword, pageable)
            .map(ArticleResponse::from);
    }
}
