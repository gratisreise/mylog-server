package com.mylog.domain.article.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.article.dto.request.ArticleCreateRequest;
import com.mylog.domain.article.dto.request.ArticleUpdateRequest;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.repository.ArticleRepository;
import com.mylog.domain.category.Category;
import com.mylog.domain.category.service.CategoryReader;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.MemberReader;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleWriter 단위 테스트")
class ArticleWriterTest {

  @Mock private ArticleRepository articleRepository;
  @Mock private MemberReader memberReader;
  @Mock private CategoryReader categoryReader;
  @Mock private AiService aiService;

  @InjectMocks private ArticleWriter articleWriter;

  private static final Long MEMBER_ID = 1L;
  private static final Long OTHER_MEMBER_ID = 2L;
  private static final Long ARTICLE_ID = 100L;
  private static final String IMAGE_URL = "https://s3.amazonaws.com/bucket/image.jpg";

  private Member createMember() {
    return Member.builder()
        .id(MEMBER_ID)
        .nickname("홍길동")
        .memberName("홍길동")
        .profileImg("https://example.com/profile.jpg")
        .build();
  }

  private Member createOtherMember() {
    return Member.builder()
        .id(OTHER_MEMBER_ID)
        .nickname("임꺽정")
        .memberName("임꺽정")
        .profileImg("https://example.com/profile2.jpg")
        .build();
  }

  private Category createCategory() {
    return Category.builder().id(1L).categoryName("일상").build();
  }

  private Article createArticle(Member member) {
    return Article.builder()
        .id(ARTICLE_ID)
        .title("테스트 제목")
        .content("테스트 내용")
        .articleImg(IMAGE_URL)
        .member(member)
        .category(createCategory())
        .build();
  }

  @Nested
  @DisplayName("게시글 생성")
  class Create {

    @Test
    @DisplayName("성공: 게시글 생성 (AI가 태그 자동 생성)")
    void create_Success() {
      // given
      ArticleCreateRequest request = new ArticleCreateRequest("테스트 제목", "테스트 내용입니다", "일상");
      Member member = createMember();
      Category category = createCategory();

      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      given(categoryReader.getByMemberIdAndCategoryName(MEMBER_ID, "일상")).willReturn(category);
      given(articleRepository.save(any(Article.class))).willAnswer(inv -> inv.getArgument(0));

      // when
      articleWriter.create(request, MEMBER_ID, IMAGE_URL);

      // then
      then(memberReader).should().getById(MEMBER_ID);
      then(categoryReader).should().getByMemberIdAndCategoryName(MEMBER_ID, "일상");
      then(articleRepository).should().save(any(Article.class));
      then(aiService).should().generateSummaryAsync(any());
    }
  }

  @Nested
  @DisplayName("게시글 수정")
  class Update {

    @Test
    @DisplayName("성공: 작성자가 게시글 수정")
    void update_ByOwner_Success() {
      // given
      Member member = createMember();
      Article article = createArticle(member);
      Category newCategory = Category.builder().id(2L).categoryName("개발").build();

      ArticleUpdateRequest request = new ArticleUpdateRequest("수정된 제목", "수정된 내용", "개발", "홍길동");

      given(articleRepository.findById(ARTICLE_ID)).willReturn(Optional.of(article));
      given(categoryReader.getByMemberIdAndCategoryName(MEMBER_ID, "개발")).willReturn(newCategory);

      // when
      articleWriter.update(request, MEMBER_ID, IMAGE_URL, ARTICLE_ID);

      // then
      then(articleRepository).should().findById(ARTICLE_ID);
      then(categoryReader).should().getByMemberIdAndCategoryName(MEMBER_ID, "개발");
      assertThat(article.getTitle()).isEqualTo("수정된 제목");
      assertThat(article.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("성공: 새 이미지 없이 수정하면 기존 이미지 유지")
    void update_WithoutNewImage_KeepOldImage() {
      // given
      Member member = createMember();
      Article article = createArticle(member);
      Category category = createCategory();

      ArticleUpdateRequest request = new ArticleUpdateRequest("수정된 제목", "수정된 내용", "일상", "홍길동");

      given(articleRepository.findById(ARTICLE_ID)).willReturn(Optional.of(article));
      given(categoryReader.getByMemberIdAndCategoryName(MEMBER_ID, "일상")).willReturn(category);

      // when
      articleWriter.update(request, MEMBER_ID, null, ARTICLE_ID);

      // then
      assertThat(article.getArticleImg()).isEqualTo(IMAGE_URL);
    }

    @Test
    @DisplayName("실패: 작성자가 아닌 사용자가 수정 시도")
    void update_ByNonOwner_ThrowsException() {
      // given
      Member owner = createMember();
      Article article = createArticle(owner);

      ArticleUpdateRequest request = new ArticleUpdateRequest("수정된 제목", "수정된 내용", "일상", "홍길동");

      given(articleRepository.findById(ARTICLE_ID)).willReturn(Optional.of(article));

      // when & then
      assertThatThrownBy(
              () -> articleWriter.update(request, OTHER_MEMBER_ID, IMAGE_URL, ARTICLE_ID))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.ACCESS_DENIED);

      then(categoryReader).should(never()).getByMemberIdAndCategoryName(any(), any());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 게시글 수정 시도")
    void update_NonExistentArticle_ThrowsException() {
      // given
      ArticleUpdateRequest request = new ArticleUpdateRequest("수정된 제목", "수정된 내용", "일상", "홍길동");

      given(articleRepository.findById(ARTICLE_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> articleWriter.update(request, MEMBER_ID, IMAGE_URL, ARTICLE_ID))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("게시글 삭제")
  class Delete {

    @Test
    @DisplayName("성공: 작성자가 게시글 삭제")
    void delete_ByOwner_Success() {
      // given
      Member member = createMember();
      Article article = createArticle(member);

      given(articleRepository.findById(ARTICLE_ID)).willReturn(Optional.of(article));
      willDoNothing().given(articleRepository).delete(article);

      // when
      articleWriter.delete(ARTICLE_ID, MEMBER_ID);

      // then
      then(articleRepository).should().findById(ARTICLE_ID);
      then(articleRepository).should().delete(article);
    }

    @Test
    @DisplayName("실패: 작성자가 아닌 사용자가 삭제 시도")
    void delete_ByNonOwner_ThrowsException() {
      // given
      Member owner = createMember();
      Article article = createArticle(owner);

      given(articleRepository.findById(ARTICLE_ID)).willReturn(Optional.of(article));

      // when & then
      assertThatThrownBy(() -> articleWriter.delete(ARTICLE_ID, OTHER_MEMBER_ID))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.ACCESS_DENIED);

      then(articleRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 게시글 삭제 시도")
    void delete_NonExistentArticle_ThrowsException() {
      // given
      given(articleRepository.findById(ARTICLE_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> articleWriter.delete(ARTICLE_ID, MEMBER_ID))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

      then(articleRepository).should(never()).delete(any());
    }
  }
}
