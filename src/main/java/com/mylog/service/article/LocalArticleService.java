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
import com.mylog.service.S3Service;
import com.mylog.service.TagService;
import java.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@ServiceType(OauthProvider.LOCAL)
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LocalArticleService implements ArticleService{

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final TagService tagService;
    private final S3Service s3Service;

    @Override
    @Transactional
    public void createArticle(ArticleCreateRequest request, CustomUser customUser, MultipartFile file)
        throws IOException {
        Category category = categoryRepository.findByCategoryName(request.getCategory())
            .orElseThrow(CMissingDataException::new);

        Member member = memberRepository.findByEmail(customUser.getUsername())
            .orElseThrow(CMissingDataException::new);

        String imageUrl = s3Service.upload(file)
            .orElseThrow(CMissingDataException::new);

        Article article = Article.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .category(category)
            .member(member)
            .articleImg(imageUrl)
            .build();

        Article savedArticle = articleRepository.save(article);

        tagService.saveTag(request.getTags(), savedArticle);
    }


    @Override
    @Transactional
    public void updateArticle(ArticleUpdateRequest request, CustomUser customUser, MultipartFile file)
        throws IOException {
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

        // 같은지 확인 -> 같으면 문자열 생성 아니면 기존꺼 사용
        String articleImg;
        if(!isSameImg(article.getArticleImg(), file.getOriginalFilename())){
            articleImg = s3Service.upload(file)
                .orElseThrow(CMissingDataException::new);
        } else {
            articleImg = article.getArticleImg();
        }

        article.update(request, category, articleImg);
    }

    private boolean isSameImg(String origin, String another) {
        return origin.substring(57).equals(another);
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

        Article article = articleRepository.findById(request.getId())
            .orElseThrow(CMissingDataException::new);

        s3Service.deleteImage(article.getArticleImg());

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
