package com.mylog.service.article;

import com.mylog.dto.article.ArticleCreateRequest;
import com.mylog.dto.article.ArticleDeleteRequest;
import com.mylog.dto.article.ArticleResponse;
import com.mylog.dto.article.ArticleUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Article;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import com.mylog.service.S3Service;
import com.mylog.service.TagService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonArticleService{
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



    //게시글 삭제

    //내 게시글 목록

    //내 게시글 검색


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
