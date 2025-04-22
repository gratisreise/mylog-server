package com.mylog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.comment.CommentCreateRequest;
import com.mylog.dto.comment.CommentResponse;
import com.mylog.dto.comment.CommentUpdateRequest;
import com.mylog.entity.Article;
import com.mylog.entity.Comment;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CommentRepository;
import com.mylog.repository.MemberRepository;
import com.mylog.service.notification.CommonNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CommonNotificationService notificationService;

    @InjectMocks
    private CommentService commentService;

    private Member member;
    private Article article;
    private Comment comment;
    private Comment childComment;
    private CustomUser customUser;
    private CommentCreateRequest createRequest;
    private CommentUpdateRequest updateRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        // 회원 설정
        member = Member.builder()
            .id(1L)
            .email("test@example.com")
            .nickname("테스터")
            .password("testPassword")
            .provider(OauthProvider.LOCAL)
            .build();

        // 게시글 설정
        article = Article.builder()
            .id(1L)
            .title("테스트 게시글")
            .content("테스트 내용")
            .member(member)
            .build();

        // 댓글 설정
        comment = Comment.builder()
            .id(1L)
            .content("테스트 댓글")
            .article(article)
            .member(member)
            .createdAt(LocalDateTime.now())
            .build();

        // 대댓글 설정
        childComment = Comment.builder()
            .id(2L)
            .content("테스트 대댓글")
            .article(article)
            .member(member)
            .parentId(1L)
            .createdAt(LocalDateTime.now())
            .build();

        // 사용자 정보 설정
        customUser = new CustomUser(member, new ArrayList<>());

        // 댓글 생성 요청 설정
        createRequest = new CommentCreateRequest();
        createRequest.setContent("테스트 댓글");
        createRequest.setArticleId(1L);
        createRequest.setParentCommentId(0);

        // 댓글 수정 요청 설정
        updateRequest = new CommentUpdateRequest();
        updateRequest.setContent("수정된 댓글");
        updateRequest.setCommentId(1L);

        // 페이징 설정
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void 댓글_생성_성공() {
        // given
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(articleRepository.findById(anyLong())).thenReturn(Optional.of(article));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        doNothing().when(notificationService).createNotificationSetting(any(), any());
        doNothing().when(notificationService).sendNotification(any(), any(), any());

        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);

        // when
        commentService.createComment(createRequest, customUser);

        // then
        verify(articleRepository).findById(createRequest.getArticleId());
        verify(memberRepository).findById(customUser.getMemberId());
        verify(commentRepository).save(commentArgumentCaptor.capture());
        verify(notificationService).createNotificationSetting(any(), any());
        verify(notificationService).sendNotification(any(), any(), any());

        Comment savedComment = commentArgumentCaptor.getValue();
        assertThat(savedComment.getContent()).isEqualTo(createRequest.getContent());
        assertThat(savedComment.getArticle()).isEqualTo(article);
        assertThat(savedComment.getMember()).isEqualTo(member);
        assertThat(savedComment.getParentId()).isEqualTo(createRequest.getParentCommentId());
    }

    @Test
    void 댓글_생성_회원없음_예외발생() {
        // given
        when(articleRepository.findById(anyLong())).thenReturn(Optional.of(article));
        when(memberRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(createRequest, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(articleRepository).findById(anyLong());
        verify(memberRepository).findById(customUser.getMemberId());
        verify(commentRepository, never()).save(any(Comment.class));
        verify(notificationService, never()).createNotificationSetting(any(), any());
        verify(notificationService, never()).sendNotification(any(), any(), any());
    }

    @Test
    void 댓글_생성_게시글없음_예외발생() {
        // given
        when(articleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(createRequest, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(articleRepository).findById(createRequest.getArticleId());
        verify(memberRepository, never()).findById(customUser.getMemberId());
        verify(commentRepository, never()).save(any(Comment.class));
        verify(notificationService, never()).createNotificationSetting(any(), any());
        verify(notificationService, never()).sendNotification(any(), any(), any());
    }

    //
    @Test
    void 대댓글_생성_성공() {
        // given
        CommentCreateRequest childRequest = new CommentCreateRequest();
        childRequest.setContent("테스트 대댓글");
        childRequest.setArticleId(1L);
        childRequest.setParentCommentId(1L);

        ArgumentCaptor<Comment> commentArgumentCaptor = ArgumentCaptor.forClass(Comment.class);

        when(articleRepository.findById(anyLong())).thenReturn(Optional.of(article));
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(member));
        when(commentRepository.save(any(Comment.class))).thenReturn(childComment);
        doNothing().when(notificationService).createNotificationSetting(any(), any());
        doNothing().when(notificationService).sendNotification(any(), any(), any());

        // when
        commentService.createComment(childRequest, customUser);

        // then
        verify(articleRepository).findById(createRequest.getArticleId());
        verify(memberRepository).findById(customUser.getMemberId());
        verify(commentRepository).save(commentArgumentCaptor.capture());
        verify(notificationService).createNotificationSetting(any(), any());
        verify(notificationService).sendNotification(any(), any(), any());

        Comment savedComment = commentArgumentCaptor.getValue();

        assertThat(savedComment.getArticle()).isEqualTo(article);
        assertThat(savedComment.getMember()).isEqualTo(member);
        assertThat(savedComment.getContent()).isEqualTo(childRequest.getContent());
        assertThat(savedComment.getParentId()).isEqualTo(childRequest.getParentCommentId());
    }

    @Test
    void 댓글_수정_성공() {
        // given
        when(commentRepository.findById(updateRequest.getCommentId()))
            .thenReturn(Optional.of(comment));
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));

        // when
        commentService.updateComment(updateRequest, customUser);

        // then
        assertThat(comment.getContent()).isEqualTo(updateRequest.getContent());
        verify(commentRepository).findById(updateRequest.getCommentId());
        verify(memberRepository).findById(customUser.getMemberId());
    }

    @Test
    void 댓글_수정_댓글없음_예외발생() {
        // given
        when(commentRepository.findById(updateRequest.getCommentId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(updateRequest, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(commentRepository).findById(updateRequest.getCommentId());
        verify(memberRepository, never()).findById(customUser.getMemberId());
    }

    @Test
    void 댓글_수정_권한없음_예외발생() {
        // given
        Member otherMember = Member.builder()
            .id(2L)
            .nickname("다른사용자")
            .password("otherPassword")
            .provider(OauthProvider.LOCAL)
            .build();

        CustomUser otherUser = new CustomUser(otherMember, new ArrayList<>());

        when(commentRepository.findById(updateRequest.getCommentId())).thenReturn(
            Optional.of(comment));
        when(memberRepository.findById(otherUser.getMemberId())).thenReturn(
            Optional.of(otherMember));

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(updateRequest, otherUser))
            .isInstanceOf(CUnAuthorizedException.class);

        verify(commentRepository).findById(updateRequest.getCommentId());
        verify(memberRepository).findById(otherUser.getMemberId());

    }

    @Test
    void 댓글_삭제_성공() {
        // given
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));
        doNothing().when(commentRepository).deleteById(commentId);

        // when
        commentService.deleteComment(commentId, customUser);

        // then
        verify(commentRepository).findById(commentId);
        verify(memberRepository).findById(customUser.getMemberId());
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    void 댓글_삭제_댓글없음_예외발생() {
        // given
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(commentId, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(commentRepository).findById(commentId);
        verify(memberRepository, never()).findById(customUser.getMemberId());
        verify(commentRepository, never()).deleteById(commentId);
    }

    @Test
    void 댓글_삭제_권한없음_예외발생() {
        // given
        Long commentId = 1L;
        Member otherMember = Member.builder()
            .id(2L)
            .nickname("otherUser")
            .password("otherPassword")
            .provider(OauthProvider.LOCAL)
            .build();
        CustomUser otherUser = new CustomUser(otherMember, new ArrayList<>());

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(memberRepository.findById(otherUser.getMemberId())).thenReturn(
            Optional.of(otherMember));

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(commentId, otherUser))
            .isInstanceOf(CUnAuthorizedException.class);

        verify(commentRepository).findById(commentId);
        verify(memberRepository).findById(otherUser.getMemberId());
        verify(commentRepository, never()).deleteById(commentId);
    }

    @Test
    void 내_댓글_조회_성공() {
        // given
        List<Comment> comments = List.of(comment);
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));
        when(commentRepository.findAllByMember(member, pageable)).thenReturn(commentPage);

        // when
        Page<CommentResponse> result = commentService.getMyComments(customUser, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(memberRepository).findById(customUser.getMemberId());
        verify(commentRepository).findAllByMember(member, pageable);
    }

    @Test
    void 내_게시글_댓글_조회_성공() {
        // given
        List<Comment> comments = List.of(comment);
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));
        when(commentRepository.findAllByArticle_Member(member, pageable)).thenReturn(commentPage);

        // when
        Page<CommentResponse> result = commentService.getComments(customUser, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(memberRepository).findById(customUser.getMemberId());
        verify(commentRepository).findAllByArticle_Member(member, pageable);
    }

    @Test
    void 게시글_댓글_조회_성공() {
        // given
        Long articleId = 1L;
        List<Comment> comments = List.of(comment);
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        when(commentRepository.findByArticle_Id(articleId, pageable)).thenReturn(commentPage);

        // when
        Page<CommentResponse> result = commentService.getComments(articleId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(commentRepository).findByArticle_Id(articleId, pageable);
    }

    @Test
    void 게시글_댓글_조회_게시글없음_예외발생() {
        // given
        Long articleId = 1L;
        when(articleRepository.existsById(articleId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> commentService.getComments(articleId, pageable))
            .isInstanceOf(CMissingDataException.class);

        verify(articleRepository).existsById(articleId);
        verify(commentRepository, never()).findByArticle_Id(anyLong(), any(Pageable.class));
    }

    @Test
    void 대댓글_조회_성공() {
        // given
        Long articleId = 1L;
        Long parentId = 1L;
        List<Comment> childComments = List.of(childComment);
        Page<Comment> commentPage = new PageImpl<>(childComments, pageable, childComments.size());

        when(articleRepository.existsById(articleId)).thenReturn(true);
        when(commentRepository.existsById(parentId)).thenReturn(true);
        when(commentRepository.findByArticle_IdAndParentId(articleId, parentId,
            pageable)).thenReturn(commentPage);

        // when
        Page<CommentResponse> result = commentService.getChildComments(articleId, parentId,
            pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(articleRepository).existsById(articleId);
        verify(commentRepository).existsById(parentId);
        verify(commentRepository).findByArticle_IdAndParentId(articleId, parentId, pageable);
    }

    @Test
    void 대댓글_조회_게시글없음_예외발생() {
        // given
        Long articleId = 1L;
        Long parentId = 1L;
        when(articleRepository.existsById(articleId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> commentService.getChildComments(articleId, parentId, pageable))
            .isInstanceOf(CMissingDataException.class);

        verify(articleRepository).existsById(articleId);
        verify(commentRepository, never()).existsById(anyLong());
        verify(commentRepository, never()).findByArticle_IdAndParentId(anyLong(), anyLong(), any(Pageable.class));
    }

    @Test
    void 대댓글_조회_부모댓글없음_예외발생() {
        // given
        Long articleId = 1L;
        Long parentId = 1L;
        when(articleRepository.existsById(articleId)).thenReturn(true);
        when(commentRepository.existsById(parentId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> commentService.getChildComments(articleId, parentId, pageable))
            .isInstanceOf(CMissingDataException.class);

        verify(articleRepository).existsById(articleId);
        verify(commentRepository).existsById(anyLong());
        verify(commentRepository, never()).findByArticle_IdAndParentId(anyLong(), anyLong(), any(Pageable.class));
    }
}