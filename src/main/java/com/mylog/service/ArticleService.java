package com.mylog.service;

import com.mylog.dto.article.ArticleCreateRequest;
import com.mylog.dto.article.ArticleDeleteRequest;
import com.mylog.dto.article.ArticleResponse;
import com.mylog.dto.article.ArticleUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Article;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final TagService tagService;
    private final S3Service s3Service;

    //게시글 생성
    @Transactional
    public void createArticle(ArticleCreateRequest request, CustomUser customUser, MultipartFile file) throws IOException{
        Category category = categoryRepository.findByCategoryName(request.getCategory())
            .orElseThrow(CMissingDataException::new);

        Member member = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);

        String imageUrl = s3Service.upload(file).orElseThrow(CMissingDataException::new);

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

    //게시글 수정
    @Transactional
    public void updateArticle(ArticleUpdateRequest request, CustomUser customUser,
        MultipartFile file) throws IOException {
        Member requestMember = memberRepository.findByNickname(request.getAuthor())
            .orElseThrow(CMissingDataException::new);
        Member userMember = memberRepository.findById(customUser.getMemberId())
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
        if(!isSame(article.getArticleImg(), file.getOriginalFilename())){
            articleImg = s3Service.upload(file).orElseThrow(CMissingDataException::new);
        } else {
            articleImg = article.getArticleImg();
        }

        tagService.saveTag(request.getTags(), article);

        article.update(request, category, articleImg);
    }

    private boolean isSame(String origin, String another) {
        return origin.substring(93).equals(another);
    }

    //게시글 삭제
    @Transactional
    public void deleteArticle(ArticleDeleteRequest request, CustomUser customUser) {
        Member requestMember = memberRepository.findByNickname(request.getAuthor())
            .orElseThrow(CMissingDataException::new);
        Member userMember = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);

        if(!requestMember.getId().equals(userMember.getId())){
            throw new CUnAuthorizedException("허용 되지 않는 유저입니다.");
        }

        Article article = articleRepository.findById(request.getId())
            .orElseThrow(CMissingDataException::new);

        s3Service.deleteImage(article.getArticleImg());

        articleRepository.deleteById(request.getId());
    }

    //내 게시글 목록
    public Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser) {
        Long memberId = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new)
            .getId();

        return articleRepository.findAllByMemberId(memberId, pageable)
            .map(ArticleResponse::from);
    }

    //내 게시글 검색
    public Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser,
        String keyword) {
        Long memberId = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new)
            .getId();
        return articleRepository
            .findByMemberIdAndTitleContainingIgnoreCase(memberId, keyword, pageable)
            .map(ArticleResponse::from);
    }


    //게시글 조회
    public ArticleResponse getArticle(Long id){
        Article article = articleRepository.findById(id)
            .orElseThrow(CMissingDataException::new);
        String category = article.getCategory().getCategoryName();
        String author = article.getMember().getNickname();
        return new ArticleResponse(article, author, category);
    };

    //전체 게시글 조회
    public Page<ArticleResponse> getArticles(Pageable pageable){
        return articleRepository.findAll(pageable)
            .map(ArticleResponse::from);
    };

    //전체 게시글 검색
    public Page<ArticleResponse> getArticles(String keyword, Pageable pageable){
        return articleRepository.findByTitleContainingIgnoreCase(keyword, pageable)
            .map(ArticleResponse::from);
    };

    //태그 검색
    public Page<ArticleResponse> getArticlesByTagName(String tagName, Pageable pageable){
        return articleRepository.findAllByTagName(tagName, pageable)
            .map(ArticleResponse::from);
    }
}
