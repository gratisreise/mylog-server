package com.mylog.controller;


import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.dto.ArticleCreateRequest;
import com.mylog.dto.ArticleResponse;
import com.mylog.dto.ArticleUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Article;
import com.mylog.repository.ArticleRepository;
import com.mylog.service.article.ArticleService;
import com.mylog.service.article.ArticleServiceFactory;
import com.mylog.service.article.CommonArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleServiceFactory factory;
    private final CommonArticleService articleService;

    //게시글 생성
    @PostMapping
    public CommonResult createArticle(
        @RequestBody ArticleCreateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        ArticleService service = factory.getMemberService(customUser.getProvider());
        service.createArticle(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //게시글 조회
    @GetMapping("/{id}")
    public SingleResult<ArticleResponse> getArticle(@PathVariable Long id){
        return ResponseService.getSingleResult(articleService.getArticle(id));
    }

    //게시글 수정
    @PutMapping
    public CommonResult updateArticle(
        @RequestBody ArticleUpdateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        ArticleService service = factory.getMemberService(customUser.getProvider());
        service.updateArticle(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //게시글 삭제


}
