
package com.mylog.service.article;

import com.mylog.exception.CUnAuthorizedException;
import com.mylog.model.dto.article.ArticleCreateRequest;
import com.mylog.model.dto.article.ArticleUpdateRequest;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Article;
import com.mylog.model.entity.Category;
import com.mylog.model.entity.Member;
import com.mylog.repository.article.ArticleRepository;
import com.mylog.repository.articletag.ArticleTagRepository;
import com.mylog.service.S3Service;
import com.mylog.service.category.CategoryReader;
import com.mylog.service.member.MemberReader;
import com.mylog.service.tag.TagService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final ArticleReader articleReader;
    private final CategoryReader categoryReader;
    private final MemberReader memberReader;
    private final ArticleTagRepository articleTagRepository;
    private final TagService tagService;
    private final S3Service s3Service;

    public void createArticle(ArticleCreateRequest request, CustomUser customUser, MultipartFile file) throws IOException{
        Member member =  memberReader.getByCustomUser(customUser);
        Category category = categoryReader.getByMemberAndCategoryName(member, request.category());
        String imageUrl = s3Service.upload(file);

        Article article = new Article(request, category, member, imageUrl);

        Article savedArticle = articleRepository.save(article);

        tagService.saveTag(request.tags(), savedArticle);
    }

    public void updateArticle(ArticleUpdateRequest request, CustomUser customUser,
        MultipartFile file, Long articleId) throws IOException {
        Member articleMember = memberReader.getByNickname(request.author());
        long requestMemberId = customUser.getMemberId();
        long articleMemberId = articleMember.getId();

        if(requestMemberId != articleMemberId){
            throw new CUnAuthorizedException("허용 되지 않는 유저입니다.");
        }

        Article article = articleReader.getArticleById(articleId);
        Category category = categoryReader.getByMemberAndCategoryName(articleMember, request.category());

        String articleImg;
        if(!isSame(article.getArticleImg(), file)){
            articleImg = s3Service.upload(file);
        } else {
            articleImg = article.getArticleImg();
        }

        tagService.saveTag(request.tags(), article);

        article.update(request, category, articleImg);
    }

    private boolean isSame(String origin, MultipartFile file) {
        if(file == null) return true;
        String another = file.getOriginalFilename();
        return origin.substring(93).equals(another);
    }

    public void deleteArticle(Long articleId, CustomUser customUser) {
        Article article = articleReader.getArticleById(articleId);
        long articleMemberId = article.getMember().getId();
        long requestMemberId = customUser.getMemberId();

        if(articleMemberId != requestMemberId){
            throw new CUnAuthorizedException("허용 되지 않는 유저입니다.");
        }

        articleTagRepository.deleteByArticle(article);

        s3Service.deleteImage(article.getArticleImg());

        articleRepository.deleteById(articleId);
    }
}
