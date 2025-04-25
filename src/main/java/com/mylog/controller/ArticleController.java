package com.mylog.controller;


import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.dto.article.ArticleCreateRequest;
import com.mylog.dto.article.ArticleDeleteRequest;
import com.mylog.dto.article.ArticleResponse;
import com.mylog.dto.article.ArticleUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.io.IOException;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleService articleService;

    //게시글 생성
    @PostMapping
    @Operation(summary = "게시글 생성")
    public CommonResult createArticle(
        @RequestPart(value = "file") MultipartFile file,
        @RequestPart(value = "request") @Valid ArticleCreateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ) throws IOException {
        articleService.createArticle(request, customUser, file);
        return ResponseService.getSuccessResult();
    }

    //게시글 조회
    @GetMapping("/{id}")
    @Operation(summary = "게시글 조회")
    public SingleResult<ArticleResponse> getArticle(@PathVariable Long id){
        return ResponseService.getSingleResult(articleService.getArticle(id));
    }

    //게시글 수정
    @PutMapping
    @Operation(summary = "게시글 수정")
    public CommonResult updateArticle(
        @RequestPart(value = "request") @Valid ArticleUpdateRequest request,
        @RequestPart(value = "file") MultipartFile file,
        @AuthenticationPrincipal CustomUser customUser
    ) throws IOException {
        articleService.updateArticle(request, customUser, file);
        return ResponseService.getSuccessResult();
    }

    //게시글 삭제
    @DeleteMapping
    @Operation(summary = "게시글 삭제")
    public CommonResult deleteArticle(
        @AuthenticationPrincipal CustomUser customUser,
        @RequestBody ArticleDeleteRequest request
    ){
        articleService.deleteArticle(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //전체 게시글 목록 조회
    @GetMapping
    @Operation(summary = "전체 게시글 목록 조회")
    public SingleResult<Page<ArticleResponse>> getArticles(
        @PageableDefault(sort="id", direction = Direction.ASC) Pageable pageable
    ){
        return ResponseService.getSingleResult(articleService.getArticles(pageable));
    }

    //내 게시글 목록 조회
    @GetMapping("/me")
    @Operation(summary = "내 게시글 목록 조회")
    public SingleResult<Page<ArticleResponse>> getArticles(
        @PageableDefault(sort="id", direction = Direction.ASC) Pageable pageable,
        @AuthenticationPrincipal CustomUser customUser
    ){
        return ResponseService.getSingleResult(articleService.getArticles(pageable, customUser));
    }

    //전체 게시글 검색
    @GetMapping("/search")
    @Operation(summary = "전체 게시글 검색")
    public SingleResult<Page<ArticleResponse>> searchArticles(
        @RequestParam String keyword,
        @PageableDefault(sort="id", direction = Direction.ASC) Pageable pageable
    ){
        return ResponseService.getSingleResult(articleService.getArticles(keyword, pageable));
    }

    //내 게시글 검색
    @GetMapping("/me/search")
    @Operation(summary = "내 게시글 목록 검색")
    public SingleResult<Page<ArticleResponse>> searchArticles(
        @RequestParam String keyword,
        @PageableDefault(sort="id", direction = Direction.ASC) Pageable pageable,
        @AuthenticationPrincipal CustomUser customUser
    ){
        return ResponseService.getSingleResult(articleService.getArticles(pageable, customUser, keyword));
    }

    @GetMapping("/tag/{tagName}")
    @Operation(summary = "태그 검색")
    public SingleResult<Page<ArticleResponse>> serchArticlesByTag(
        @PathVariable String tagName,
        @PageableDefault(sort="id", direction = Direction.ASC) Pageable pageable
    ){
        return ResponseService.getSingleResult(
            articleService.getArticlesByTagName(tagName, pageable));
    }
}
