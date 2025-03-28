package com.mylog.service.article;

import com.mylog.dto.ArticleCreateRequest;
import com.mylog.dto.ArticleDeleteRequest;
import com.mylog.dto.ArticleResponse;
import com.mylog.dto.ArticleUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.repository.ArticleRepository;
import io.sentry.MeasurementUnit.Custom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


public interface ArticleService {

    // 게시글 생성
    void createArticle(ArticleCreateRequest request, CustomUser customUser);

    //게시글 수정
    void updateArticle(ArticleUpdateRequest request, CustomUser customUser);

    //게시글 삭제
    void deleteArticle(ArticleDeleteRequest request, CustomUser customUser);

    //내 게시글 목록
    Page<ArticleResponse> getArticles(Pageable pageable, CustomUser customUser);

    //내 게시글 검색
    Page<ArticleResponse> getArticles(Pageable pageable, Custom customUser, String keyword);

}
