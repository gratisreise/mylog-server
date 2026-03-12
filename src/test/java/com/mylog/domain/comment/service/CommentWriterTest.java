package com.mylog.domain.comment.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.article.entity.Article;
import com.mylog.domain.article.service.ArticleReader;
import com.mylog.domain.comment.dto.CommentCreateRequest;
import com.mylog.domain.comment.dto.CommentUpdateRequest;
import com.mylog.domain.comment.entity.Comment;
import com.mylog.domain.comment.repository.CommentRepository;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.MemberReader;
import com.mylog.domain.member.service.NotificationSettingWriter;
import com.mylog.domain.notification.service.NotificationWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentWriter 단위 테스트")
class CommentWriterTest {

  @Mock private CommentRepository commentRepository;
  @Mock private CommentReader commentReader;
  @Mock private MemberReader memberReader;
  @Mock private ArticleReader articleReader;
  @Mock private NotificationWriter notificationWriter;
  @Mock private NotificationSettingWriter notificationSettingWriter;

  @InjectMocks private CommentWriter commentWriter;
  private static final Long MEMBER_ID = 1L;
  private static final Long OTHER_MEMBER_ID = 2L;
  private static final Long ARTICLE_ID = 1L;
  private static final Long COMMENT_ID = 1L;
  private static final String CONTENT = "테스트 댓글 내용입니다.";
  private static final String UPDATED_CONTENT = "수정된 댓글 내용입니다.";
  private static final String NICKNAME = "테스트유저";
  private static final String EMAIL = "test@example.com";

  @Nested
  @DisplayName("createComment 메서드")
  class CreateComment {
    @Test
    @DisplayName("성공: 댓글 정상 생성 (알림 발송 포함)")
    void createComment_Success() {
      // given
      Member member = createMember(MEMBER_ID);
      Member articleAuthor = createMember(OTHER_MEMBER_ID);
      Article article = createArticle(articleAuthor);
      CommentCreateRequest request = new CommentCreateRequest(CONTENT, 0L);
      given(articleReader.getArticleById(ARTICLE_ID)).willReturn(article);
      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
      given(commentRepository.save(commentCaptor.capture()))
          .willAnswer(
              invocation -> {
                Comment savedComment = invocation.getArgument(0);
                return Comment.builder()
                    .id(COMMENT_ID)
                    .article(savedComment.getArticle())
                    .member(savedComment.getMember())
                    .content(savedComment.getContent())
                    .parent(savedComment.getParent())
                    .build();
              });
      // when
      Long result = commentWriter.createComment(ARTICLE_ID, request, MEMBER_ID);
      Comment savedComment = commentCaptor.getValue();
      // then
      assertThat(result).isEqualTo(COMMENT_ID);
      assertThat(savedComment.getArticle()).isEqualTo(article);
      assertThat(savedComment.getMember()).isEqualTo(member);
      assertThat(savedComment.getContent()).isEqualTo(CONTENT);
      then(articleReader).should().getArticleById(ARTICLE_ID);
      then(memberReader).should().getById(MEMBER_ID);
      then(commentRepository).should().save(savedComment);
      then(notificationSettingWriter).should().createNotificationSetting(articleAuthor, "comment");
      then(notificationWriter).should().sendNotification(articleAuthor, ARTICLE_ID, "comment");
    }
  }

  @Nested
  @DisplayName("updateComment 메서드")
  class UpdateComment {
    @Test
    @DisplayName("성공: 본인 댓글 정상 수정")
    void updateComment_Success() {
      // given
      Member member = createMember(MEMBER_ID);
      Article article = createArticle(member);
      Comment comment = createComment(article, member);
      given(commentReader.getById(COMMENT_ID)).willReturn(comment);
      CommentUpdateRequest request = new CommentUpdateRequest(UPDATED_CONTENT);
      // when
      commentWriter.updateComment(request, MEMBER_ID, COMMENT_ID);
      // then
      assertThat(comment.getContent()).isEqualTo(UPDATED_CONTENT);
      then(commentReader).should().getById(COMMENT_ID);
    }

    @Test
    @DisplayName("실패: 타인의 댓글 수정 시도 → COMMENT_FORBIDDEN 예외")
    void updateComment_Forbidden() {
      // given
      Member owner = createMember(MEMBER_ID);
      Article article = createArticle(owner);
      Comment comment = createComment(article, owner);
      given(commentReader.getById(COMMENT_ID)).willReturn(comment);
      CommentUpdateRequest request = new CommentUpdateRequest(UPDATED_CONTENT);
      // when & then
      assertThatThrownBy(() -> commentWriter.updateComment(request, OTHER_MEMBER_ID, COMMENT_ID))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.COMMENT_FORBIDDEN);
      then(commentReader).should().getById(COMMENT_ID);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 댓글 → COMMENT_NOT_FOUND 예외")
    void updateComment_NotFound() {
      // given
      CommentUpdateRequest request = new CommentUpdateRequest(UPDATED_CONTENT);
      given(commentReader.getById(COMMENT_ID))
          .willThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
      // when & then
      assertThatThrownBy(() -> commentWriter.updateComment(request, MEMBER_ID, COMMENT_ID))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
      then(commentReader).should().getById(COMMENT_ID);
    }
  }

  @Nested
  @DisplayName("deleteComment 메서드")
  class DeleteComment {
    @Test
    @DisplayName("성공: 본인 댓글 정상 삭제")
    void deleteComment_Success() {
      // given
      Member member = createMember(MEMBER_ID);
      Article article = createArticle(member);
      Comment comment = createComment(article, member);
      given(commentReader.getById(COMMENT_ID)).willReturn(comment);
      willDoNothing().given(commentRepository).deleteById(COMMENT_ID);
      // when
      commentWriter.deleteComment(COMMENT_ID, MEMBER_ID);
      // then
      then(commentReader).should().getById(COMMENT_ID);
      then(commentRepository).should().deleteById(COMMENT_ID);
    }

    @Test
    @DisplayName("실패: 타인의 댓글 삭제 시도 → COMMENT_FORBIDDEN 예외")
    void deleteComment_Forbidden() {
      // given
      Member owner = createMember(MEMBER_ID);
      Article article = createArticle(owner);
      Comment comment = createComment(article, owner);
      given(commentReader.getById(COMMENT_ID)).willReturn(comment);
      // when & then
      assertThatThrownBy(() -> commentWriter.deleteComment(COMMENT_ID, OTHER_MEMBER_ID))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.COMMENT_FORBIDDEN);
      then(commentReader).should().getById(COMMENT_ID);
      then(commentRepository).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 댓글 → COMMENT_NOT_FOUND 예외")
    void deleteComment_NotFound() {
      // given
      given(commentReader.getById(COMMENT_ID))
          .willThrow(new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
      // when & then
      assertThatThrownBy(() -> commentWriter.deleteComment(COMMENT_ID, MEMBER_ID))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
      then(commentReader).should().getById(COMMENT_ID);
      then(commentRepository).should(never()).deleteById(any());
    }
  }

  private Member createMember(Long id) {
    return Member.builder()
        .id(id)
        .email(id + EMAIL)
        .nickname(NICKNAME + id)
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
