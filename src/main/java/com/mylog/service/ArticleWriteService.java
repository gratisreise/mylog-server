
package com.mylog.service;

import com.mylog.model.dto.article.ArticleCreateRequest;
import com.mylog.model.dto.article.ArticleUpdateRequest;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Article;
import com.mylog.model.entity.Category;
import com.mylog.model.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.ArticleTagRepository;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleWriteService {
    private final ArticleRepository articleRepository;
    private final ArticleTagRepository articleTagRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final TagService tagService;
    private final S3Service s3Service;

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

    public void updateArticle(ArticleUpdateRequest request, CustomUser customUser,
        MultipartFile file, Long articleId) throws IOException {
        Member requestMember = memberRepository.findByNickname(request.getAuthor())
            .orElseThrow(CMissingDataException::new);
        Member userMember = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);

        if(!requestMember.getId().equals(userMember.getId())){
            throw new CUnAuthorizedException("허용 되지 않는 유저입니다.");
        }

        Article article = articleRepository.findById(articleId)
            .orElseThrow(CMissingDataException::new);
        Category category = categoryRepository.findByCategoryName(request.getCategory())
            .orElseThrow(CMissingDataException::new);

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

    public void deleteArticle(Long articleId, CustomUser customUser) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(CMissingDataException::new);
        long requestMember = memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new).getId();
        long userMember = article.getMember().getId();

        if(requestMember != userMember){
            throw new CUnAuthorizedException("허용 되지 않는 유저입니다.");
        }

        articleTagRepository.deleteByArticle(article);

        s3Service.deleteImage(article.getArticleImg());

        articleRepository.deleteById(articleId);
    }
}
