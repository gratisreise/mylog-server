package com.mylog.controller;


import com.mylog.common.CommonResult;
import com.mylog.common.ResponseService;
import com.mylog.dto.ArticleCreateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.repository.ArticleRepository;
import com.mylog.service.article.ArticleService;
import com.mylog.service.article.ArticleServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleServiceFactory factory;

    //게시글 생성
    @PostMapping
    public CommonResult createArticle(
        @RequestBody ArticleCreateRequest request,
        CustomUser customUser
    ){
        ArticleService service = factory.getMemberService(customUser.getProvider());
        service.createArticle(request, customUser);
        return ResponseService.getSuccessResult();
    }

    //게시글 조회

    //게시글 수정

    //게시글 삭제


}
