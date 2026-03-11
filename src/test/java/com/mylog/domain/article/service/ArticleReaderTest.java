package com.mylog.domain.article.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.mylog.common.exception.BusinessException;
import com.mylog.domain.article.dto.request.ArticleSearchRequest;
import com.mylog.domain.article.dto.response.ArticleResponse;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.entity.ArticleTag;
import com.mylog.domain.article.entity.Tag;
import com.mylog.domain.article.repository.ArticleRepository;
import com.mylog.domain.article.repository.ArticleTagRepository;
import com.mylog.domain.category.Category;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.MemberReader;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleReader 단위 테스트")
class ArticleReaderTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private ArticleTagRepository articleTagRepository;
  @Mock private MemberReader memberReader;

  @InjectMocks private ArticleReader articleReader;

  private static final Long MEMBER_ID = 1L;
  private static final Long ARTICLE_ID = 100L;
  private static final Long CATEGORY_ID = 10L;
  private static final String CATEGORY_NAME = "일상";

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
        .articleImg("https://example.com/image.jpg")
        .member(createMember())
        .category(createCategory())
        .build();
  }

  @Nested
  @DisplayName("전체 게시글 목록 조회")
  class GetArticles {

    @Test
    @DisplayName("성공: 전체 게시글 목록 조회")
    void getArticles_All_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      ArticleResponse response =
          new ArticleResponse(ARTICLE_ID, "제목", "내용", "홍길동", "일상", null, List.of(), null, null);
      Page<ArticleResponse> expectedPage = new PageImpl<>(List.of(response));

      given(articleRepository.findAllCustom(pageable)).willReturn(expectedPage);

      // when
      Page<ArticleResponse> result = articleReader.getArticles(pageable);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(1);
      then(articleRepository).should().findAllCustom(pageable);
    }
  }

  @Nested
  @DisplayName("회원별 게시글 목록 조회")
  class GetArticlesByMember {

    @Test
    @DisplayName("성공: 회원의 게시글 목록 조회")
    void getArticles_ByMember_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      Member member = createMember();
      ArticleResponse response =
          new ArticleResponse(ARTICLE_ID, "제목", "내용", "홍길동", "일상", null, List.of(), null, null);
      Page<ArticleResponse> expectedPage = new PageImpl<>(List.of(response));

      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      given(articleRepository.findMineByMember(member, pageable)).willReturn(expectedPage);

      // when
      Page<ArticleResponse> result = articleReader.getArticles(pageable, MEMBER_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(1);
      then(memberReader).should().getById(MEMBER_ID);
      then(articleRepository).should().findMineByMember(member, pageable);
    }
  }

  @Nested
  @DisplayName("게시글 검색")
  class Search {

    @Test
    @DisplayName("성공: 키워드로 전체 검색")
    void search_AllByKeyword_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      ArticleSearchRequest request = new ArticleSearchRequest("테스트", null, null, null, pageable);

      ArticleResponse response =
          new ArticleResponse(ARTICLE_ID, "제목", "내용", "홍길동", "일상", null, List.of(), null, null);
      Page<ArticleResponse> expectedPage = new PageImpl<>(List.of(response));

      given(articleRepository.searchAllByTitle("테스트", pageable)).willReturn(expectedPage);

      // when
      Page<ArticleResponse> result = articleReader.search(request);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(1);
      then(articleRepository).should().searchAllByTitle("테스트", pageable);
    }

    @Test
    @DisplayName("성공: 태그로 전체 검색")
    void search_AllByTag_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      ArticleSearchRequest request = new ArticleSearchRequest(null, "태그1", null, null, pageable);

      ArticleResponse response =
          new ArticleResponse(ARTICLE_ID, "제목", "내용", "홍길동", "일상", null, List.of(), null, null);
      Page<ArticleResponse> expectedPage = new PageImpl<>(List.of(response));

      given(articleRepository.searchAllByTagName("태그1", pageable)).willReturn(expectedPage);

      // when
      Page<ArticleResponse> result = articleReader.search(request);

      // then
      assertThat(result).isNotNull();
      then(articleRepository).should().searchAllByTagName("태그1", pageable);
    }

    @Test
    @DisplayName("성공: 카테고리로 전체 검색")
    void search_AllByCategory_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      ArticleSearchRequest request =
          new ArticleSearchRequest(null, null, CATEGORY_ID, null, pageable);

      ArticleResponse response =
          new ArticleResponse(ARTICLE_ID, "제목", "내용", "홍길동", "일상", null, List.of(), null, null);
      Page<ArticleResponse> expectedPage = new PageImpl<>(List.of(response));

      given(articleRepository.findAllByCategory(CATEGORY_ID, pageable)).willReturn(expectedPage);

      // when
      Page<ArticleResponse> result = articleReader.search(request);

      // then
      assertThat(result).isNotNull();
      then(articleRepository).should().findAllByCategory(CATEGORY_ID, pageable);
    }

    @Test
    @DisplayName("성공: 검색 조건 없으면 전체 목록 조회")
    void search_NoCondition_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      ArticleSearchRequest request = new ArticleSearchRequest(null, null, null, null, pageable);

      ArticleResponse response =
          new ArticleResponse(ARTICLE_ID, "제목", "내용", "홍길동", "일상", null, List.of(), null, null);
      Page<ArticleResponse> expectedPage = new PageImpl<>(List.of(response));

      given(articleRepository.findAllCustom(pageable)).willReturn(expectedPage);

      // when
      Page<ArticleResponse> result = articleReader.search(request);

      // then
      assertThat(result).isNotNull();
      then(articleRepository).should().findAllCustom(pageable);
    }

    @Test
    @DisplayName("성공: 내 게시글 키워드 검색")
    void search_MineByKeyword_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      Member member = createMember();
      ArticleSearchRequest request =
          new ArticleSearchRequest("테스트", null, null, MEMBER_ID, pageable);

      ArticleResponse response =
          new ArticleResponse(ARTICLE_ID, "제목", "내용", "홍길동", "일상", null, List.of(), null, null);
      Page<ArticleResponse> expectedPage = new PageImpl<>(List.of(response));

      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      given(articleRepository.searchMineByTitle(member, "테스트", pageable)).willReturn(expectedPage);

      // when
      Page<ArticleResponse> result = articleReader.search(request);

      // then
      assertThat(result).isNotNull();
      then(memberReader).should().getById(MEMBER_ID);
      then(articleRepository).should().searchMineByTitle(member, "테스트", pageable);
    }

    @Test
    @DisplayName("성공: 내 게시글 태그 검색")
    void search_MineByTag_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      Member member = createMember();
      ArticleSearchRequest request =
          new ArticleSearchRequest(null, "태그1", null, MEMBER_ID, pageable);

      ArticleResponse response =
          new ArticleResponse(ARTICLE_ID, "제목", "내용", "홍길동", "일상", null, List.of(), null, null);
      Page<ArticleResponse> expectedPage = new PageImpl<>(List.of(response));

      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      given(articleRepository.searchMineByTagName(member, "태그1", pageable))
          .willReturn(expectedPage);

      // when
      Page<ArticleResponse> result = articleReader.search(request);

      // then
      assertThat(result).isNotNull();
      then(articleRepository).should().searchMineByTagName(member, "태그1", pageable);
    }

    @Test
    @DisplayName("성공: 내 게시글 카테고리 검색")
    void search_MineByCategory_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      Member member = createMember();
      ArticleSearchRequest request =
          new ArticleSearchRequest(null, null, CATEGORY_ID, MEMBER_ID, pageable);

      ArticleResponse response =
          new ArticleResponse(ARTICLE_ID, "제목", "내용", "홍길동", "일상", null, List.of(), null, null);
      Page<ArticleResponse> expectedPage = new PageImpl<>(List.of(response));

      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      given(articleRepository.findMineByMemberAndCategory(member, CATEGORY_ID, pageable))
          .willReturn(expectedPage);

      // when
      Page<ArticleResponse> result = articleReader.search(request);

      // then
      assertThat(result).isNotNull();
      then(articleRepository).should().findMineByMemberAndCategory(member, CATEGORY_ID, pageable);
    }

    @Test
    @DisplayName("성공: 내 게시글 검색 조건 없으면 내 전체 목록 조회")
    void search_MineNoCondition_Success() {
      // given
      Pageable pageable = PageRequest.of(0, 10);
      Member member = createMember();
      ArticleSearchRequest request =
          new ArticleSearchRequest(null, null, null, MEMBER_ID, pageable);

      ArticleResponse response =
          new ArticleResponse(ARTICLE_ID, "제목", "내용", "홍길동", "일상", null, List.of(), null, null);
      Page<ArticleResponse> expectedPage = new PageImpl<>(List.of(response));

      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      given(articleRepository.findMineByMember(member, pageable)).willReturn(expectedPage);

      // when
      Page<ArticleResponse> result = articleReader.search(request);

      // then
      assertThat(result).isNotNull();
      then(articleRepository).should().findMineByMember(member, pageable);
    }
  }

  @Nested
  @DisplayName("게시글 상세 조회")
  class GetArticle {

    @Test
    @DisplayName("성공: 게시글 상세 조회")
    void getArticle_Success() {
      // given
      Article article = createArticle();
      Tag tag = Tag.from("테스트태그");
      ArticleTag articleTag = ArticleTag.builder().article(article).tag(tag).build();

      given(articleRepository.findById(ARTICLE_ID)).willReturn(Optional.of(article));
      given(articleTagRepository.findAllByArticleId(ARTICLE_ID)).willReturn(List.of(articleTag));

      // when
      ArticleResponse result = articleReader.getArticle(ARTICLE_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(ARTICLE_ID);
      assertThat(result.tags()).containsExactly("테스트태그");
      then(articleRepository).should().findById(ARTICLE_ID);
      then(articleTagRepository).should().findAllByArticleId(ARTICLE_ID);
    }

    @Test
    @DisplayName("실패: 게시글이 없으면 예외 발생")
    void getArticle_NotFound_ThrowsException() {
      // given
      given(articleRepository.findById(ARTICLE_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> articleReader.getArticle(ARTICLE_ID))
          .isInstanceOf(BusinessException.class);
      then(articleRepository).should().findById(ARTICLE_ID);
    }
  }

  @Nested
  @DisplayName("게시글 존재 여부 확인")
  class IsExists {

    @Test
    @DisplayName("성공: 게시글 존재함")
    void isExists_True() {
      // given
      given(articleRepository.existsById(ARTICLE_ID)).willReturn(true);

      // when
      boolean result = articleReader.isExists(ARTICLE_ID);

      // then
      assertThat(result).isTrue();
      then(articleRepository).should().existsById(ARTICLE_ID);
    }

    @Test
    @DisplayName("성공: 게시글 존재하지 않음")
    void isExists_False() {
      // given
      given(articleRepository.existsById(ARTICLE_ID)).willReturn(false);

      // when
      boolean result = articleReader.isExists(ARTICLE_ID);

      // then
      assertThat(result).isFalse();
      then(articleRepository).should().existsById(ARTICLE_ID);
    }
  }

  @Nested
  @DisplayName("게시글 엔티티 조회")
  class GetArticleById {

    @Test
    @DisplayName("성공: 게시글 엔티티 조회")
    void getArticleById_Success() {
      // given
      Article article = createArticle();
      given(articleRepository.findById(ARTICLE_ID)).willReturn(Optional.of(article));

      // when
      Article result = articleReader.getArticleById(ARTICLE_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(ARTICLE_ID);
      then(articleRepository).should().findById(ARTICLE_ID);
    }

    @Test
    @DisplayName("성공: 게시글이 없으면 null 반환")
    void getArticleById_NotFound_ReturnsNull() {
      // given
      given(articleRepository.findById(ARTICLE_ID)).willReturn(Optional.empty());

      // when
      Article result = articleReader.getArticleById(ARTICLE_ID);

      // then
      assertThat(result).isNull();
      then(articleRepository).should().findById(ARTICLE_ID);
    }
  }
}
