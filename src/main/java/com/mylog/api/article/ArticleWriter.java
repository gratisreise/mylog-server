
package com.mylog.api.article;

import com.mylog.domain.entity.Article;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Category;
import com.mylog.domain.entity.Member;
import com.mylog.service.S3Service;
import com.mylog.service.articletag.ArticleTagWriter;
import com.mylog.service.category.CategoryReader;
import com.mylog.api.member.MemberReader;
import com.mylog.service.tag.TagService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleWriter {
    private final ArticleRepository articleRepository;
    private final ArticleReader articleReader;
    private final CategoryReader categoryReader;
    private final MemberReader memberReader;
    private final ArticleTagWriter articleTagWriter;
    private final TagService tagService;
    private final S3Service s3Service;

    public void createArticle(ArticleCreateRequest request, CustomUser customUser, MultipartFile file) throws IOException{
        Member member =  memberReader.getByCustomUser(customUser);
        Category category = categoryReader.getByMemberIdAndCategoryName(member.getId(), request.category());
        String imageUrl = s3Service.upload(file);

        Article article = request.toEntity(member, category, imageUrl);

        Article savedArticle = articleRepository.save(article);

        tagService.saveTag(request.tags(), savedArticle);
    }

    public void updateArticle(
        ArticleUpdateRequest request, CustomUser customUser,
        MultipartFile file, Long articleId) throws IOException {
        Article article = articleReader.getArticleById(articleId);


        if(article.isOwnedBy(customUser.getMemberId())){
            throw new CUnAuthorizedException("허용 되지 않는 유저입니다.");
        }

        Category category = categoryReader.getByMemberIdAndCategoryName(article.getMember().getId(), request.category());

        String articleImg = getArticleImg(file, article);

        tagService.saveTag(request.tags(), article);

        article.update(request, category, articleImg);
    }

    public void deleteArticle(Long articleId, CustomUser customUser) {
        Article article = articleReader.getArticleById(articleId);

        if(article.isOwnedBy(customUser.getMemberId())){
            throw new CUnAuthorizedException("허용 되지 않는 유저입니다.");
        }


        articleTagWriter.deleteArticleTag(article);

        s3Service.deleteImage(article.getArticleImg());

        articleRepository.deleteById(articleId);
    }

    private String getArticleImg(MultipartFile file, Article article) throws IOException {
        String articleImg;
        if(!isSame(article.getArticleImg(), file)){
            articleImg = s3Service.upload(file);
        } else {
            articleImg = article.getArticleImg();
        }
        return articleImg;
    }

    private boolean isSame(String origin, MultipartFile file) {
        if(file == null) return true;
        String another = file.getOriginalFilename();
        return origin.substring(93).equals(another);
    }

}
