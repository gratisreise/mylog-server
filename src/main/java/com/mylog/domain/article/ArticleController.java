package com.mylog.domain.article;

import com.mylog.common.annotations.MemberId;
import com.mylog.common.response.PageResponse;
import com.mylog.domain.article.dto.response.ArticleResponse;
import com.mylog.external.s3.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final S3Service s3Service;

    //게시글 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 생성")
    public CommonResult createArticle(
        @RequestPart(value = "file") MultipartFile file,
        @RequestPart(value = "request") @Valid ArticleCreateRequest request,
        @MemberId Long memberId
    ){
        String imageUrl = s3Service.upload(file); //s3 이미지 생성
        articleService.createArticle(request, memberId, imageUrl);
        return ResponseService.getSuccessResult();
    }

    //게시글 수정
    @PutMapping("/{articleId}")
    @Operation(summary = "게시글 수정")
    public CommonResult updateArticle(
        @RequestPart(value = "request") @Valid ArticleUpdateRequest request,
        @RequestPart(required = false, value = "file") MultipartFile file,
        @AuthenticationPrincipal CustomUser customUser,
        @PathVariable Long articleId
    ){
        String imageUrl = s3Service.upload(file); //s3이미지 생성
        articleService.updateArticle(request, customUser, imageUrl, articleId);
        return ResponseService.getSuccessResult();
    }

    //게시글 삭제
    @DeleteMapping("/{articleId}")
    @Operation(summary = "게시글 삭제")
    public CommonResult deleteArticle(
        @AuthenticationPrincipal CustomUser customUser,
        @PathVariable Long articleId
    ){
        articleService.deleteArticle(articleId, customUser);
        return ResponseService.getSuccessResult();
    }

    @GetMapping("/{articleId}")
    @Operation(summary = "게시글 상세")
    public SingleResult<ArticleResponse> getArticle(@PathVariable Long articleId){
        return ResponseService.getSingleResult(articleService.getArticle(articleId));
    }

    @GetMapping("/all")
    @Operation(summary = "전체 게시글 목록 조회")
    public SingleResult<PageResponse<ArticleResponse>> getArticles(
        @PageableDefault Pageable pageable){
        return ResponseService.getSingleResult(articleService.getArticles(pageable));
    }

    @GetMapping("/me")
    @Operation(summary = "내 게시글 목록 조회")
    public SingleResult<PageResponse<ArticleResponse>> getArticles(
        @PageableDefault(sort="id", direction = Direction.ASC) Pageable pageable,
        @AuthenticationPrincipal CustomUser customUser
    ){
        return ResponseService.getSingleResult(articleService.getArticles(pageable, customUser));
    }

    @GetMapping("/all/search")
    @Operation(summary = "전체 게시글 검색")
    public SingleResult<PageResponse<ArticleResponse>> searchArticles(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String tag,
        @PageableDefault Pageable pageable
    ){
        return ResponseService.getSingleResult(articleService.getArticles(keyword, tag, pageable));
    }

    //내 게시글 검색
    @GetMapping("/me/search")
    @Operation(summary = "내 게시글 검색")
    public SingleResult<PageResponse<ArticleResponse>> searchArticles(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String tag,
        @PageableDefault Pageable pageable,
        @AuthenticationPrincipal CustomUser customUser
    ){
        return ResponseService
            .getSingleResult(articleService.getArticles(keyword, tag, pageable, customUser));
    }

}
