package com.mylog.domain.comment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.service.ArticleReader;
import com.mylog.domain.comment.dto.CommentResponse;
import com.mylog.domain.comment.entity.Comment;
import com.mylog.domain.comment.repository.CommentRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentReader 단위 테스트")
class CommentReaderTest {

  @Mock private CommentRepository commentRepository;
  @Mock private MemberReader memberReader;
  @Mock private ArticleReader articleReader;

  @InjectMocks private CommentReader commentReader;

  private static final Long MEMBER_ID = 1L;
  private static final Long ARTICLE_ID = 1L;
  private static final Long COMMENT_ID = 1L;
  private static final String CONTENT = "테스트 댓글 내용입니다.";
  private static final String NICKNAME = "테스트유저";
  private static final String EMAIL = "test@example.com";

  @Nested
  @DisplayName("getById 메서드")
  class GetById {

    @Test
    @DisplayName("성공: 댓글 ID로 정상 조회")
    void getById_Success() {
      // given
      Member member = createMember();
      Article article = createArticle(member);
      Comment comment = createComment(article, member);
      given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.of(comment));

      // when
      Comment result = commentReader.getById(COMMENT_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(COMMENT_ID);
      assertThat(result.getContent()).isEqualTo(CONTENT);
      then(commentRepository).should().findById(COMMENT_ID);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 댓글 ID면 COMMENT_NOT_FOUND 예외")
    void getById_NotFound() {
      // given
      given(commentRepository.findById(COMMENT_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> commentReader.getById(COMMENT_ID))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);

      then(commentRepository).should().findById(COMMENT_ID);
    }
  }

  @Nested
  @DisplayName("getMyComments 메서드")
  class GetMyComments {

    @Test
    @DisplayName("성공: 회원의 댓글 목록 정상 조회 (페이징)")
    void getMyComments_Success() {
      // given
      Member member = createMember();
      Article article = createArticle(member);
      Comment comment1 = createComment(article, member);
      Comment comment2 =
          Comment.builder()
              .id(2L)
              .article(article)
              .member(member)
              .content("두 번째 댓글입니다.")
              .parent(null)
              .build();

      Pageable pageable = PageRequest.of(0, 10);
      Page<Comment> commentPage = new PageImpl<>(java.util.List.of(comment1, comment2));

      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      given(commentRepository.findAllByMember(member, pageable)).willReturn(commentPage);

      // when
      Page<CommentResponse> result = commentReader.getMyComments(MEMBER_ID, pageable);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(2);
      assertThat(result.getContent().get(0).content()).isEqualTo(CONTENT);
      then(memberReader).should().getById(MEMBER_ID);
      then(commentRepository).should().findAllByMember(member, pageable);
    }
  }

  private Member createMember() {
    return Member.builder()
        .id(MEMBER_ID)
        .email(EMAIL)
        .nickname(NICKNAME)
        .memberName("테스트")
        .profileImg("https://example.com/profile.jpg")
        .build();
  }

  private Article createArticle(Member member) {
    return Article.builder()
        .id(ARTICLE_ID)
        .member(member)
        .title("테스트 게시글")
        .content("테스트 게시글 내용입니다.")
        .build();
  }

  private Comment createComment(Article article, Member member) {
    return Comment.builder()
        .id(COMMENT_ID)
        .article(article)
        .member(member)
        .content(CONTENT)
        .parent(null)
        .build();
  }
}
