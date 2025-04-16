package com.mylog.service.article;

import com.mylog.dto.article.ArticleCreateRequest;
import com.mylog.dto.article.ArticleDeleteRequest;
import com.mylog.dto.article.ArticleResponse;
import com.mylog.dto.article.ArticleUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import java.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;


public interface ArticleService {

    // 게시글 생성
    void createArticle(ArticleCreateRequest request, CustomUser customUser, MultipartFile file)
        throws IOException;

    //게시글 수정
    void updateArticle(ArticleUpdateRequest request, CustomUser customUser, MultipartFile file)
        throws IOException;

    //게시글 삭제
    void deleteArticle(ArticleDeleteRequest request, CustomUser customUser);

    //내 게시글 목록
    Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser);

    //내 게시글 검색
    Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser, String keyword);

}
