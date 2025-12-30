
package com.mylog.article.service;


import com.mylog.article.entity.Article;
import com.mylog.article.repository.ArticleRepository;
import com.mylog.exception.CUnAuthorizedException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleWriter {
    private final ArticleRepository articleRepository;

    public void createArticle(Article article){
        articleRepository.save(article);
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

        tagWriter.saveTag(request.tags(), article);

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
