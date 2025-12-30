package com.mylog.article.service;


import com.mylog.api.auth.CustomUser;
import com.mylog.article.dto.ArticleCreateRequest;
import com.mylog.article.dto.ArticleResponse;
import com.mylog.article.entity.Article;
import com.mylog.category.entity.Category;
import com.mylog.category.service.CategoryReader;
import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
import com.mylog.tag.service.TagReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleService {
    private final ArticleReader articleReader;
    private final ArticleWriter articleWriter;
    private final MemberReader memberReader;
    private final CategoryReader categoryReader;
    private final TagReader tagReader;


    @Transactional
    public void createArticle(
        ArticleCreateRequest request, CustomUser customUser, String imageUrl
    ){

        //멤버객체 가져오기
        Member member = memberReader.getById(customUser.getMemberId());

        //카테고리 객체 가져오기
        Category category = categoryReader.getByMemberIdAndCategoryName(member.getId(), request.category());

        //게시글 생성
        Article article = request.toEntity(member, category, imageUrl);

        articleWriter.createArticle(article);
    }




    //게시글 조회
    public ArticleResponse getArticle(Long articleId) {
        Article article  = articleReader.getById(articleId);
        List<String> tags = tagReader.getTags(article);
        return ArticleResponse.of(article, tags);
    }
}
