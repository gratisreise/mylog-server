package com.mylog.api.article;


import com.mylog.common.CommonResult;
import com.mylog.common.ListResult;
import com.mylog.common.ResponseService;
import com.mylog.common.SingleResult;
import com.mylog.model.dto.classes.CustomUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ArticleReader articleReader;
    private final ArticleWriter articleWriter;

    //게시글 생성
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "게시글 생성")
    public CommonResult createArticle(
        @RequestPart(value = "file") MultipartFile file,
        @RequestPart(value = "request") @Valid ArticleCreateRequest request,
        @AuthenticationPrincipal CustomUser customUser
    ) throws IOException {
        articleWriter.createArticle(request, customUser, file);
        return ResponseService.getSuccessResult();
    }

    //게시글 조회
    @GetMapping("/{articleId}")
    @Operation(summary = "게시글 조회")
    public SingleResult<ArticleResponse> getArticle(@PathVariable Long articleId){
        return ResponseService.getSingleResult(articleReader.getArticle(articleId));
    }

    //게시글 수정
    @PutMapping("/{articleId}")
    @Operation(summary = "게시글 수정")
    public CommonResult updateArticle(
        @RequestPart(value = "request") @Valid ArticleUpdateRequest request,
        @RequestPart(required = false, value = "file") MultipartFile file,
        @AuthenticationPrincipal CustomUser customUser,
        @PathVariable Long articleId
    ) throws IOException {
        articleWriter.updateArticle(request, customUser, file, articleId);
        return ResponseService.getSuccessResult();
    }

    //게시글 삭제
    @DeleteMapping("/{articleId}")
    @Operation(summary = "게시글 삭제")
    public CommonResult deleteArticle(
        @AuthenticationPrincipal CustomUser customUser,
        @PathVariable Long articleId
    ){
        articleWriter.deleteArticle(articleId, customUser);
        return ResponseService.getSuccessResult();
    }

//    //전체 게시글 목록 조회
//    @GetMapping("/all")
//    @Operation(summary = "전체 게시글 목록 조회")
//    public SingleResult<PageResponse<ArticleResponse>> getArticles(
//        @PageableDefault(sort="id", direction = Direction.ASC, page=150) Pageable pageable){
//        return ResponseService.getSingleResult(articleReader.getArticles(pageable));
//    }





    @GetMapping("/all")
    @Operation(summary = "전체 게시글 목록 조회")
    public ListResult<ArticleTestResponse> getArticles(
        @PageableDefault(sort="id", direction = Direction.ASC, page=80, size= 1000) Pageable pageable){
        return ResponseService.getListResult(articleReader.getArticles(pageable));
    }

    //내 게시글 목록 조회
    @GetMapping("/me")
    @Operation(summary = "내 게시글 목록 조회")
    public SingleResult<Page<ArticleResponse>> getArticles(
        @PageableDefault(sort="id", direction = Direction.ASC) Pageable pageable,
        @AuthenticationPrincipal CustomUser customUser
    ){
        return ResponseService.getSingleResult(articleReader.getArticles(pageable, customUser));
    }

    //전체 게시글 검색
    @GetMapping("/all/search")
    @Operation(summary = "전체 게시글 검색")
    public SingleResult<Page<ArticleResponse>> searchArticles(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String tag,
        @PageableDefault Pageable pageable
    ){
        return ResponseService.getSingleResult(articleReader.getArticles(keyword, tag, pageable));
    }

    //내 게시글 검색
    @GetMapping("/me/search")
    @Operation(summary = "내 게시글 검색")
    public SingleResult<Page<ArticleResponse>> searchArticles(
        @RequestParam String keyword,
        @PageableDefault Pageable pageable,
        @AuthenticationPrincipal CustomUser customUser
    ){
        return ResponseService.getSingleResult(
            articleReader.getArticles(pageable, customUser, keyword));
    }

}