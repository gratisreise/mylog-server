package com.mylog.controller;


import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.dto.article.ArticleCreateRequest;
import com.mylog.dto.article.ArticleDeleteRequest;
import com.mylog.dto.article.ArticleResponse;
import com.mylog.dto.article.ArticleUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.service.article.ArticleService;
import com.mylog.service.article.ArticleServiceFactory;
import com.mylog.service.article.CommonArticleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
        @RequestBody @Valid ArticleUpdateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ){
        ArticleService service = factory.getMemberService(customUser.getProvider());
        service.updateArticle(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //게시글 삭제
    @DeleteMapping
    public CommonResult deleteArticle(
        @AuthenticationPrincipal CustomUser customUser,
        @RequestBody ArticleDeleteRequest request
    ){
        ArticleService service = factory.getMemberService(customUser.getProvider());
        service.deleteArticle(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //전체 게시글 목록 조회
    @GetMapping
    public SingleResult<Page<ArticleResponse>> getArticles(
        @PageableDefault(sort="id", direction = Direction.ASC) Pageable pageable
    ){
        return ResponseService.getSingleResult(articleService.getArticles(pageable));
    }

    //내 게시글 목록 조회
    @GetMapping("/me")
    public SingleResult<Page<ArticleResponse>> getArticles(
        @PageableDefault(sort="id", direction = Direction.ASC) Pageable pageable,
        @AuthenticationPrincipal CustomUser customUser
    ){
        ArticleService service = factory.getMemberService(customUser.getProvider());
        return ResponseService.getSingleResult(service.getArticles(pageable, customUser));
    }

    //전체 게시글 검색
    @GetMapping("/search")
    public SingleResult<Page<ArticleResponse>> searchArticles(
        @RequestParam String keyword,
        @PageableDefault(sort="id", direction = Direction.ASC) Pageable pageable
    ){
        return ResponseService.getSingleResult(articleService.getArticles(keyword, pageable));
    }

    //내 게시글 검색
    public SingleResult<Page<ArticleResponse>> searchArticles(
        @RequestParam String keyword,
        @PageableDefault(sort="id", direction = Direction.ASC) Pageable pageable,
        @AuthenticationPrincipal CustomUser customUser
    ){
        ArticleService service = factory.getMemberService(customUser.getProvider());
        return ResponseService.getSingleResult(service.getArticles(pageable, customUser, keyword));
    }
}
