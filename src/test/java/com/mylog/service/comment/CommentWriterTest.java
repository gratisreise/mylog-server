package com.mylog.service.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.api.comment.CommentReader;
import com.mylog.api.comment.CommentWriter;
import com.mylog.domain.enums.OauthProvider;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.api.comment.CommentCreateRequest;
import com.mylog.api.comment.CommentUpdateRequest;
import com.mylog.domain.entity.Article;
import com.mylog.domain.entity.Category;
import com.mylog.domain.entity.Comment;
import com.mylog.domain.entity.Member;
import com.mylog.api.comment.CommentRepository;
import com.mylog.api.article.ArticleReader;
import com.mylog.api.member.MemberReader;
import com.mylog.api.notification.NotificationWriter;
import com.mylog.api.notificationsetting.NotificationSettingWriter;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentWriterTest {

    @InjectMocks
    private CommentWriter commentWriter;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentReader commentReader;

    @Mock
    private MemberReader memberReader;

    @Mock
    private ArticleReader articleReader;

    @Mock
    private NotificationWriter notificationWriter;

    @Mock
    private NotificationSettingWriter notificationSettingWriter;

    private CustomUser customUser;
    private Member testMember;
    private Member testMember2;
    private Category testCategory;
    private Article testArticle;
    private Comment testComment;
    private CommentCreateRequest commentCreateRequest;
    private CommentUpdateRequest commentUpdateRequest;

    @BeforeEach
    void setUp() {
        // Create test members
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .memberName("Test User")
                .nickname("testuser")
                .password("password")
                .provider(OauthProvider.LOCAL)
                .providerId("test@example.com" + OauthProvider.LOCAL)
                .build();

        testMember2 = Member.builder()
                .id(2L)
                .email("test2@example.com")
                .memberName("Test User 2")
                .nickname("testuser2")
                .password("password")
                .provider(OauthProvider.LOCAL)
                .providerId("test2@example.com" + OauthProvider.LOCAL)
                .build();

        // Create test category
        testCategory = Category.builder()
                .id(1L)
                .categoryName("Test Category")
                .member(testMember)
                .build();

        // Create test article
        testArticle = Article.builder()
                .id(1L)
                .title("Test Article")
                .content("Test content")
                .member(testMember)
                .category(testCategory)
                .build();

        // Create test comment
        testComment = Comment.builder()
                .id(1L)
                .content("Test comment")
                .article(testArticle)
                .member(testMember)
                .parentId(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        customUser = new CustomUser(testMember, Collections.emptyList());
        
        commentCreateRequest = new CommentCreateRequest("새로운 댓글 내용", 0L);
        commentUpdateRequest = new CommentUpdateRequest("수정된 댓글 내용");
    }

    @Test
    void createComment_성공() {
        // Given
        Long articleId = 1L;
        
        when(articleReader.getArticleById(articleId)).thenReturn(testArticle);
        when(memberReader.getById(1L)).thenReturn(testMember);

        // When
        commentWriter.createComment(articleId, commentCreateRequest, customUser);

        // Then
        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        
        Comment savedComment = commentCaptor.getValue();
        assertThat(savedComment.getContent()).isEqualTo("새로운 댓글 내용");
        assertThat(savedComment.getArticle()).isEqualTo(testArticle);
        assertThat(savedComment.getMember()).isEqualTo(testMember);
        assertThat(savedComment.getParentId()).isEqualTo(0L);
        
        verify(articleReader).getArticleById(articleId);
        verify(memberReader).getById(1L);
    }

    @Test
    void updateComment_성공() {
        // Given
        Long commentId = 1L;
        
        when(commentReader.getById(commentId)).thenReturn(testComment);

        // When
        commentWriter.updateComment(commentUpdateRequest, customUser, commentId);

        // Then
        verify(commentReader).getById(commentId);
    }

    @Test
    void updateComment_권한_없음() {
        // Given
        Long commentId = 2L;
        Comment commentByOther = Comment.builder()
                .id(commentId)
                .content("다른 사용자 댓글")
                .article(testArticle)
                .member(testMember2)
                .parentId(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(commentReader.getById(commentId)).thenReturn(commentByOther);

        // When & Then
        assertThatThrownBy(() -> commentWriter.updateComment(commentUpdateRequest, customUser, commentId))
                .isInstanceOf(CUnAuthorizedException.class)
                .hasMessage("허용되지 않는 유저입니다.");

        verify(commentReader).getById(commentId);
    }

    @Test
    void deleteComment_댓글_작성자_권한으로_삭제_성공() {
        // Given
        Long commentId = 1L;
        
        when(commentReader.getById(commentId)).thenReturn(testComment);

        // When
        commentWriter.deleteComment(commentId, customUser);

        // Then
        verify(commentRepository).deleteById(commentId);
        verify(commentReader).getById(commentId);
    }

    @Test
    void deleteComment_게시글_작성자_권한으로_삭제_성공() {
        // Given
        Long commentId = 2L;
        
        // testMember2가 작성한 댓글이지만, testMember가 게시글 작성자인 경우
        Comment commentOnMyArticle = Comment.builder()
                .id(commentId)
                .content("내 게시글의 다른 사용자 댓글")
                .article(testArticle) // testMember가 작성한 게시글
                .member(testMember2) // testMember2가 작성한 댓글
                .parentId(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(commentReader.getById(commentId)).thenReturn(commentOnMyArticle);

        // When
        commentWriter.deleteComment(commentId, customUser);

        // Then - 게시글 작성자가 다른 사용자의 댓글을 삭제할 수 있음
        verify(commentRepository).deleteById(commentId);
        verify(commentReader).getById(commentId);
    }

    @Test
    void deleteComment_권한_없음() {
        // Given
        Long commentId = 2L;
        
        // 다른 사용자의 게시글에 다른 사용자가 작성한 댓글 (testMember는 권한 없음)
        Article articleByOther = Article.builder()
                .id(2L)
                .title("다른 사용자 게시글")
                .member(testMember2)
                .category(testCategory)
                .build();
        
        Comment otherComment = Comment.builder()
                .id(commentId)
                .content("다른 사용자의 댓글")
                .article(articleByOther) // testMember2가 작성한 게시글
                .member(testMember2) // testMember2가 작성한 댓글
                .parentId(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(commentReader.getById(commentId)).thenReturn(otherComment);

        // When & Then
        assertThatThrownBy(() -> commentWriter.deleteComment(commentId, customUser))
                .isInstanceOf(CUnAuthorizedException.class)
                .hasMessage("허용되지 않는 유저입니다.");

        verify(commentReader).getById(commentId);
        verify(commentRepository, never()).deleteById(commentId);
    }
}