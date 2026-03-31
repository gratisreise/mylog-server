package com.mylog.domain.article.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.mylog.common.enums.AnalyzeStatus;
import com.mylog.common.enums.WritingStyle;
import com.mylog.common.response.PageResponse;
import com.mylog.domain.article.ArticleService;
import com.mylog.domain.article.dto.request.ArticleCreateRequest;
import com.mylog.domain.article.dto.request.ArticleQueryParam;
import com.mylog.domain.article.dto.request.ArticleUpdateRequest;
import com.mylog.domain.article.dto.request.StyleTransformRequest;
import com.mylog.domain.article.dto.response.ArticleCreateResponse;
import com.mylog.domain.article.dto.response.ArticleResponse;
import com.mylog.domain.article.dto.response.ArticleSummaryResponse;
import com.mylog.domain.article.dto.response.StyleTransformResponse;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.category.Category;
import com.mylog.domain.member.entity.Member;
import com.mylog.external.s3.S3Service;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleService 단위 테스트")
class ArticleServiceTest {

  @Mock private ArticleReader articleReader;
  @Mock private ArticleWriter articleWriter;
  @Mock private S3Service s3Service;
  @Mock private AiService aiService;

  @InjectMocks private ArticleService articleService;

  private static final Long MEMBER_ID = 1L;
  private static final Long ARTICLE_ID = 100L;
  private static final Long CATEGORY_ID = 10L;
  private static final String IMAGE_URL = "https://s3.amazonaws.com/bucket/image.jpg";

  private Member createMember() {
    return Member.builder()
        .id(MEMBER_ID)
        .nickname("홍길동")
        .memberName("홍길동")
        .profileImg("https://example.com/profile.jpg")
        .build();
  }

  private Category createCategory() {
    return Category.builder().id(CATEGORY_ID).categoryName("일상").build();
  }

  private Article createArticle() {
    return Article.builder()
        .id(ARTICLE_ID)
        .title("테스트 제목")
        .content("테스트 내용")
        .articleImg(IMAGE_URL)
        .member(createMember())
        .category(createCategory())
        .build();
  }

  @Nested
  @DisplayName("게시글 생성")
  class CreateArticle {

    @Test
    @DisplayName("성공: 이미지 업로드 후 게시글 생성")
    void createArticle_Success() {
      // given
      ArticleCreateRequest request = new ArticleCreateRequest("테스트 제목", "테스트 내용입니다", "일상");
      MultipartFile file = mock(MultipartFile.class);
      Article savedArticle = createArticle();

      given(s3Service.upload(file)).willReturn(IMAGE_URL);
      given(articleWriter.create(request, MEMBER_ID, IMAGE_URL)).willReturn(savedArticle);

      // when
      ArticleCreateResponse result = articleService.createArticle(request, MEMBER_ID, file);

      // then
      assertThat(result).isNotNull();
      assertThat(result.articleId()).isEqualTo(ARTICLE_ID);
      then(s3Service).should().upload(file);
      then(articleWriter).should().create(request, MEMBER_ID, IMAGE_URL);
    }
  }

  @Nested
  @DisplayName("게시글 수정")
  class UpdateArticle {

    @Test
    @DisplayName("성공: 이미지 업로드 후 수정")
    void updateArticle_Success() {
      // given
      ArticleUpdateRequest request = new ArticleUpdateRequest("수정된 제목", "수정된 내용", "일상");
      MultipartFile file = mock(MultipartFile.class);

      given(s3Service.upload(file)).willReturn(IMAGE_URL);
      willDoNothing().given(articleWriter).update(request, MEMBER_ID, IMAGE_URL, ARTICLE_ID);

      // when
      articleService.updateArticle(request, MEMBER_ID, file, ARTICLE_ID);

      // then
      then(s3Service).should().upload(file);
      then(articleWriter).should().update(request, MEMBER_ID, IMAGE_URL, ARTICLE_ID);
    }
  }

  @Nested
  @DisplayName("게시글 삭제")
  class DeleteArticle {

    @Test
    @DisplayName("성공: 게시글 삭제 위임")
    void deleteArticle_Success() {
      // given
      willDoNothing().given(articleWriter).delete(ARTICLE_ID, MEMBER_ID);

      // when
      articleService.deleteArticle(ARTICLE_ID, MEMBER_ID);

      // then
      then(articleWriter).should().delete(ARTICLE_ID, MEMBER_ID);
    }
  }

  @Nested
  @DisplayName("게시글 상세 조회")
  class GetArticle {

    @Test
    @DisplayName("성공: 게시글 상세 조회")
    void getArticle_Success() {
      // given
      ArticleResponse expectedResponse =
          new ArticleResponse(
              ARTICLE_ID, "제목", "내용", "홍길동", "일상", IMAGE_URL, List.of("태그1"), null, null);

      given(articleReader.getArticle(ARTICLE_ID)).willReturn(expectedResponse);

      // when
      ArticleResponse result = articleService.getArticle(ARTICLE_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(ARTICLE_ID);
      then(articleReader).should().getArticle(ARTICLE_ID);
    }
  }

  @Nested
  @DisplayName("통합 게시글 목록/검색 조회")
  class GetArticles {

    @Test
    @DisplayName("성공: 전체 게시글 목록 조회 (필터 없음)")
    void getArticles_All_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      ArticleResponse response =
          new ArticleResponse(
              ARTICLE_ID, "제목", "내용", "홍길동", "일상", IMAGE_URL, List.of(), null, null);
      Page<ArticleResponse> page = new PageImpl<>(List.of(response));

      given(articleReader.getArticles(any(ArticleQueryParam.class), eq(pageable))).willReturn(page);

      // when
      PageResponse<ArticleResponse> result =
          articleService.getArticles(pageable, null, null, null, null);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContents()).hasSize(1);
      then(articleReader).should().getArticles(any(ArticleQueryParam.class), eq(pageable));
    }

    @Test
    @DisplayName("성공: 내 게시글 목록 조회")
    void getArticles_My_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      ArticleResponse response =
          new ArticleResponse(
              ARTICLE_ID, "제목", "내용", "홍길동", "일상", IMAGE_URL, List.of(), null, null);
      Page<ArticleResponse> page = new PageImpl<>(List.of(response));

      given(articleReader.getArticles(any(ArticleQueryParam.class), eq(pageable))).willReturn(page);

      // when
      PageResponse<ArticleResponse> result =
          articleService.getArticles(pageable, MEMBER_ID, null, null, null);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContents()).hasSize(1);
      then(articleReader).should().getArticles(any(ArticleQueryParam.class), eq(pageable));
    }

    @Test
    @DisplayName("성공: 키워드로 검색")
    void getArticles_ByKeyword_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      String keyword = "테스트";
      ArticleResponse response =
          new ArticleResponse(
              ARTICLE_ID, "제목", "내용", "홍길동", "일상", IMAGE_URL, List.of(), null, null);
      Page<ArticleResponse> page = new PageImpl<>(List.of(response));

      given(articleReader.getArticles(any(ArticleQueryParam.class), eq(pageable))).willReturn(page);

      // when
      PageResponse<ArticleResponse> result =
          articleService.getArticles(pageable, null, keyword, null, null);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContents()).hasSize(1);
      then(articleReader).should().getArticles(any(ArticleQueryParam.class), eq(pageable));
    }

    @Test
    @DisplayName("성공: 태그로 검색")
    void getArticles_ByTag_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      String tag = "태그1";
      ArticleResponse response =
          new ArticleResponse(
              ARTICLE_ID, "제목", "내용", "홍길동", "일상", IMAGE_URL, List.of(), null, null);
      Page<ArticleResponse> page = new PageImpl<>(List.of(response));

      given(articleReader.getArticles(any(ArticleQueryParam.class), eq(pageable))).willReturn(page);

      // when
      PageResponse<ArticleResponse> result =
          articleService.getArticles(pageable, null, null, tag, null);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContents()).hasSize(1);
      then(articleReader).should().getArticles(any(ArticleQueryParam.class), eq(pageable));
    }

    @Test
    @DisplayName("성공: 카테고리로 검색")
    void getArticles_ByCategory_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      ArticleResponse response =
          new ArticleResponse(
              ARTICLE_ID, "제목", "내용", "홍길동", "일상", IMAGE_URL, List.of(), null, null);
      Page<ArticleResponse> page = new PageImpl<>(List.of(response));

      given(articleReader.getArticles(any(ArticleQueryParam.class), eq(pageable))).willReturn(page);

      // when
      PageResponse<ArticleResponse> result =
          articleService.getArticles(pageable, null, null, null, CATEGORY_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContents()).hasSize(1);
      then(articleReader).should().getArticles(any(ArticleQueryParam.class), eq(pageable));
    }

    @Test
    @DisplayName("성공: 복합 필터 검색 (키워드 + 카테고리)")
    void getArticles_ByKeywordAndCategory_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      String keyword = "테스트";
      ArticleResponse response =
          new ArticleResponse(
              ARTICLE_ID, "제목", "내용", "홍길동", "일상", IMAGE_URL, List.of(), null, null);
      Page<ArticleResponse> page = new PageImpl<>(List.of(response));

      given(articleReader.getArticles(any(ArticleQueryParam.class), eq(pageable))).willReturn(page);

      // when
      PageResponse<ArticleResponse> result =
          articleService.getArticles(pageable, null, keyword, null, CATEGORY_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContents()).hasSize(1);
      then(articleReader).should().getArticles(any(ArticleQueryParam.class), eq(pageable));
    }

    @Test
    @DisplayName("성공: 내 게시글 복합 필터 검색")
    void getArticles_MyWithFilters_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      String keyword = "테스트";
      String tag = "태그1";
      ArticleResponse response =
          new ArticleResponse(
              ARTICLE_ID, "제목", "내용", "홍길동", "일상", IMAGE_URL, List.of(), null, null);
      Page<ArticleResponse> page = new PageImpl<>(List.of(response));

      given(articleReader.getArticles(any(ArticleQueryParam.class), eq(pageable))).willReturn(page);

      // when
      PageResponse<ArticleResponse> result =
          articleService.getArticles(pageable, MEMBER_ID, keyword, tag, CATEGORY_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContents()).hasSize(1);
      then(articleReader).should().getArticles(any(ArticleQueryParam.class), eq(pageable));
    }
  }

  @Nested
  @DisplayName("AI 문체 변환")
  class TransformWritingStyle {

    @Test
    @DisplayName("성공: 문체 변환")
    void transformWritingStyle_Success() {
      // given
      StyleTransformRequest request =
          new StyleTransformRequest("테스트 내용입니다", WritingStyle.FRIENDLY, null);
      String transformedContent = "변환된 내용이에요~ 😊";

      given(aiService.transformWritingStyle("테스트 내용입니다", WritingStyle.FRIENDLY))
          .willReturn(transformedContent);

      // when
      StyleTransformResponse result = articleService.transformWritingStyle(request, MEMBER_ID);

      // then
      assertThat(result.transformedContent()).isEqualTo(transformedContent);
      assertThat(result.writingStyle()).isEqualTo("FRIENDLY");
      then(aiService).should().transformWritingStyle("테스트 내용입니다", WritingStyle.FRIENDLY);
    }
  }

  @Nested
  @DisplayName("AI 요약 조회")
  class GetArticleSummary {

    @Test
    @DisplayName("성공: 요약 조회")
    void getArticleSummary_Success() {
      // given
      Article article =
          Article.builder()
              .id(ARTICLE_ID)
              .title("테스트 제목")
              .content("테스트 내용")
              .aiSummary("AI가 생성한 요약입니다.")
              .aiSummaryStatus(AnalyzeStatus.COMPLETED)
              .build();

      given(articleReader.getArticleById(ARTICLE_ID)).willReturn(article);

      // when
      ArticleSummaryResponse result = articleService.getArticleSummary(ARTICLE_ID);

      // then
      assertThat(result.articleId()).isEqualTo(ARTICLE_ID);
      assertThat(result.aiSummary()).isEqualTo("AI가 생성한 요약입니다.");
      assertThat(result.status()).isEqualTo(AnalyzeStatus.COMPLETED);
      then(articleReader).should().getArticleById(ARTICLE_ID);
    }

    @Test
    @DisplayName("성공: 요약 대기 상태")
    void getArticleSummary_Pending() {
      // given
      Article article =
          Article.builder()
              .id(ARTICLE_ID)
              .title("테스트 제목")
              .content("테스트 내용")
              .aiSummary(null)
              .aiSummaryStatus(AnalyzeStatus.PENDING)
              .build();

      given(articleReader.getArticleById(ARTICLE_ID)).willReturn(article);

      // when
      ArticleSummaryResponse result = articleService.getArticleSummary(ARTICLE_ID);

      // then
      assertThat(result.articleId()).isEqualTo(ARTICLE_ID);
      assertThat(result.aiSummary()).isNull();
      assertThat(result.status()).isEqualTo(AnalyzeStatus.PENDING);
    }
  }
}
