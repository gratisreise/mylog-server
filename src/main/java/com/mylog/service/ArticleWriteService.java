
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
    private final ArticleReadService articleReadService;
    private final CategoryReadService categoryReadService;
    private final MemberReadService memberReadService;
    private final ArticleTagRepository articleTagRepository;
    private final TagService tagService;
    private final S3Service s3Service;

    public void createArticle(ArticleCreateRequest request, CustomUser customUser, MultipartFile file) throws IOException{

        Category category = categoryReadService.getByCategoryName(request.getCategory());
        Member member =  memberReadService.getById(customUser.getMemberId());
        String imageUrl = s3Service.upload(file).orElseThrow(CMissingDataException::new);

        Article article = new Article(request, category, member, imageUrl);

        Article savedArticle = articleRepository.save(article);

        tagService.saveTag(request.getTags(), savedArticle);
    }

    public void updateArticle(ArticleUpdateRequest request, CustomUser customUser,
        MultipartFile file, Long articleId) throws IOException {
        long requestMemberId = customUser.getMemberId();
        long articleMemberId = memberReadService.getByNickname(request.getAuthor()).getId();

        if(requestMemberId != articleMemberId){
            throw new CUnAuthorizedException("허용 되지 않는 유저입니다.");
        }

        Article article = articleReadService.getArticleById(articleId);
        Category category = categoryReadService.getByCategoryName(request.getCategory());

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
        Article article = articleReadService.getArticleById(articleId);
        long articleMemberId = article.getMember().getId();
        long requestMemberId = memberReadService.getById(customUser.getMemberId()).getId();

        if(articleMemberId != requestMemberId){
            throw new CUnAuthorizedException("허용 되지 않는 유저입니다.");
        }

        articleTagRepository.deleteByArticle(article);

        s3Service.deleteImage(article.getArticleImg());

        articleRepository.deleteById(articleId);
    }
}
