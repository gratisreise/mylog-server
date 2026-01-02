package com.mylog.article.service;


import com.mylog.api.auth.CustomUser;
import com.mylog.article.dto.ArticleCreateRequest;
import com.mylog.article.dto.ArticleResponse;
import com.mylog.article.dto.ArticleUpdateRequest;
import com.mylog.article.entity.Article;
import com.mylog.article.projections.ArticleProjection;
import com.mylog.category.entity.Category;
import com.mylog.category.service.CategoryReader;
import com.mylog.common.PageResponse;
import com.mylog.enums.ErrorMessage;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
import com.mylog.s3.S3Service;
import com.mylog.tag.entity.Tag;
import com.mylog.tag.service.TagReader;
import com.mylog.tag.service.TagWriter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {
    private final ArticleReader articleReader;
    private final ArticleWriter articleWriter;
    private final ArticleTagWriter articleTagWriter;
    private final MemberReader memberReader;
    private final CategoryReader categoryReader;
    private final TagReader tagReader;
    private final S3Service s3Service;
    private final TagWriter tagWriter;


    @Transactional
    public void createArticle(
        ArticleCreateRequest request, CustomUser customUser, String imageUrl
    ){

        //전처리 정보 모음
        Member member = memberReader.getById(customUser.getMemberId());
        Category category = categoryReader.getByMemberIdAndCategoryName(member.getId(), request.category());

        //게시글 생성
        Article article = request.toEntity(member, category, imageUrl);
        Article savedArticle = articleWriter.createArticle(article);

        //태그리스트
        createTag(request.tagNames(), savedArticle);
    }

    @Transactional
    public void updateArticle(ArticleUpdateRequest request, CustomUser customUser,
        String image, Long articleId) {

        Article article = articleReader.getById(articleId);
        Long memberId = customUser.getMemberId();

        if(!article.isOwnedBy(memberId)){
            throw new CUnAuthorizedException(ErrorMessage.NOT_YOUR_ARICLE);
        }

        //기존 이미지 s3에서 삭제
        s3Service.deleteImage(article.getArticleImg());

        //카테고리 가져오기
        Category category = categoryReader.getByMemberIdAndCategoryName(memberId, request.category());

        //게시글 업데이트
        Article updateArticle = request.toEntity(image, category);
        article.update(updateArticle);

        //기존 태그 삭제
        articleTagWriter.deleteArticleTag(article);

        createTag(request.tagNames(), article);
    }


    /** 게시글 삭제
     * s3 이미지 삭제
     * article 삭제
     */
    @Transactional
    public void deleteArticle(Long articleId, CustomUser customUser) {

        Article article = articleReader.getById(articleId);
        Long memberId = customUser.getMemberId();

        if(article.isOwnedBy(memberId)){
            throw new CUnAuthorizedException(ErrorMessage.NOT_YOUR_ARICLE);
        }

        s3Service.deleteImage(article.getArticleImg());
        articleWriter.deleteArticle(articleId);
    }

    //게시글 조회
    public ArticleResponse getArticle(Long articleId) {
        Article article  = articleReader.getById(articleId);
        List<String> tags = tagReader.getTags(article);
        return ArticleResponse.of(article, tags);
    }

    /** 게시글 목록조회
     * 게시글 리스트 가져오기
     * 게시글 마다 태그 리스트 가져오기 => N+1 문제 발생
     * QueryDSL로 쿼리 작성으로 조회 => N+1 2개의 쿼리로 작성완료
     */
    public PageResponse<ArticleResponse> getArticles(Pageable pageable) {
        Page<ArticleResponse> response =  articleReader.getArticles(pageable)
            .map(ArticleResponse::from);
        return PageResponse.from(response);
    }

    //내 게시글 목록조회
    public PageResponse<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser) {
        Long memberId = customUser.getMemberId();
        Page<ArticleResponse> response = articleReader.getArticles(memberId, pageable)
            .map(ArticleResponse::from);
        return PageResponse.from(response);
    }


    private void createTag(List<String> request, Article article) {
        //태그리스트
        List<Tag> tags = tagWriter.getTagsOrCreate(request);

        //게시글 태그 생성
        articleTagWriter.createArticleTag(article, tags);
    }
}
