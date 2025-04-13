package com.mylog.service.comment;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.comment.CommentCreateRequest;
import com.mylog.dto.comment.CommentResponse;
import com.mylog.dto.comment.CommentUpdateRequest;
import com.mylog.entity.Article;
import com.mylog.entity.Comment;
import com.mylog.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CommentRepository;
import com.mylog.repository.MemberRepository;
import com.mylog.service.notification.CommonNotificationService;
import com.mylog.service.notification.NotificationService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
class LocalCommentServiceTest {

    @InjectMocks
    private LocalCommentService commentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CommonNotificationService notificationService;

    private Member testMember;
    private Article testArticle;
    private Comment testComment;
    private Comment testChildComment;
    private CustomUser customUser;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .id(1L)
            .memberName("테스트유저")
            .nickname("테스트닉네임")
            .build();

        testArticle = Article.builder()
            .id(1L)
            .title("테스트제목")
            .content("테스트내용")
            .member(testMember)
            .build();

        testComment = Comment.builder()
            .id(1L)
            .content("테스트댓글")
            .article(testArticle)
            .member(testMember)
            .build();

        testChildComment = Comment.builder()
            .id(2L)
            .content("테스트답글")
            .article(testArticle)
            .parentId(testComment.getId())
            .member(testMember)
            .build();

        customUser = new CustomUser("1", Collections.emptyList());
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void 댓글_생성_성공() {
        // given
        CommentCreateRequest request = new CommentCreateRequest();
        request.setArticleId(1L);
        request.setContent("테스트댓글");

        when(articleRepository.findById(request.getArticleId()))
            .thenReturn(Optional.of(testArticle));
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));
        when(commentRepository.save(any(Comment.class)))
            .thenReturn(testComment);

        // when & then
        assertThatCode(() -> commentService.createComment(request, customUser))
            .doesNotThrowAnyException();

        verify(articleRepository).findById(request.getArticleId());
        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(commentRepository).save(any(Comment.class));
        verify(notificationService).createNotificationSetting(testArticle.getMember(), "comment");
        verify(notificationService).sendNotification(testArticle.getMember(), testArticle.getId(), "comment");
    }

    @Test
    void 댓글_생성_실패_게시글없음() {
        // given
        CommentCreateRequest request = new CommentCreateRequest();
        request.setArticleId(999L);
        request.setContent("테스트댓글");

        when(articleRepository.findById(request.getArticleId()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(request, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(articleRepository).findById(request.getArticleId());
        verify(memberRepository, never()).findByEmail(any());
        verify(commentRepository, never()).save(any());
        verify(notificationService, never()).createNotificationSetting(any(), any());
        verify(notificationService, never()).sendNotification(any(), any(), any());
    }

    @Test
    void 댓글_생성_실패_회원정보없음() {
        // given
        CommentCreateRequest request = new CommentCreateRequest();
        request.setArticleId(1L);
        request.setContent("테스트댓글");

        when(articleRepository.findById(request.getArticleId()))
            .thenReturn(Optional.of(testArticle));
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.createComment(request, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(articleRepository).findById(request.getArticleId());
        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(commentRepository, never()).save(any());
        verify(notificationService, never()).createNotificationSetting(any(), any());
        verify(notificationService, never()).sendNotification(any(), any(), any());
    }

    @Test
    void 댓글_수정_성공() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setContent("수정된 댓글");
        Long commentId = 1L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));

        // when & then
        assertThatCode(() -> commentService.updateComment(request, commentId, customUser))
            .doesNotThrowAnyException();

        verify(commentRepository).findById(commentId);
        assertThat(testComment.getContent()).isEqualTo("수정된 댓글");
    }

    @Test
    void 댓글_수정_실패_댓글없음() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setContent("수정된 댓글");
        Long commentId = 999L;

        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(request, commentId, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(commentRepository).findById(commentId);
    }

    @Test
    void 댓글_수정_실패_권한없음() {
        // given
        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setContent("수정된 댓글");
        Long commentId = 1L;

        Member otherMember = Member.builder()
            .id(2L)
            .memberName("다른사용자")
            .nickname("다른닉네임")
            .build();

        Comment otherComment = Comment.builder()
            .id(commentId)
            .content("원본 댓글")
            .article(testArticle)
            .member(otherMember)
            .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(otherComment));
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));

        // when & then
        assertThatThrownBy(() -> commentService.updateComment(request, commentId, customUser))
            .isInstanceOf(CUnAuthorizedException.class)
            .hasMessage("허용되지 않는 유저입니다.");

        verify(commentRepository).findById(commentId);
        assertThat(otherComment.getContent()).isEqualTo("원본 댓글");
    }

    @Test
    void 댓글_삭제_성공() {
        // given
        Long commentId = 1L;

        when(commentRepository.findById(commentId))
            .thenReturn(Optional.of(testComment));
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));

        // when & then
        assertThatCode(() -> commentService.deleteComment(commentId, customUser))
            .doesNotThrowAnyException();

        verify(commentRepository).findById(commentId);
        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(commentRepository).deleteById(commentId);
    }

    @Test
    void 댓글_삭제_실패_댓글없음() {
        // given
        Long commentId = 999L;

        when(commentRepository.findById(commentId))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(commentId, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(commentRepository).findById(commentId);
        verify(memberRepository, never()).findByEmail(any());
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void 댓글_삭제_실패_권한없음() {
        // given
        Long commentId = 1L;

        Member otherMember = Member.builder()
            .id(2L)
            .memberName("다른사용자")
            .nickname("다른닉네임")
            .build();
        Member member = Member.builder()
            .id(3L)
            .build();
        Article otherArticle = Article.builder()
            .member(member)
            .build();

        Comment otherComment = Comment.builder()
            .id(commentId)
            .content("다른사용자의 댓글")
            .article(otherArticle) // 3L
            .member(otherMember) //2L
            .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(otherComment));
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(commentId, customUser))
            .isInstanceOf(CUnAuthorizedException.class)
            .hasMessage("허용되지 않는 유저입니다.");

        verify(commentRepository).findById(commentId);
        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(commentRepository, never()).deleteById(any());
    }

    @Test
    void 내_댓글_목록_조회_성공() {
        // given
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));

        List<Comment> comments = Arrays.asList(testComment, testChildComment);
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        when(commentRepository.findAllByMember(testMember, pageable))
            .thenReturn(commentPage);

        // when
        Page<CommentResponse> result = commentService.getMyComments(customUser, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
            .extracting("content", "author")
            .containsExactly(
                tuple("테스트댓글", "테스트닉네임"),
                tuple("테스트답글", "테스트닉네임")
            );

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(commentRepository).findAllByMember(testMember, pageable);
    }

    @Test
    void 내_댓글_목록_조회_실패_회원정보없음() {
        // given
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getMyComments(customUser, pageable))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(commentRepository, never()).findAllByMember(any(), any());
    }

    @Test
    void 내_댓글_목록_조회_성공_댓글없음() {
        // given
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));

        Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(commentRepository.findAllByMember(testMember, pageable))
            .thenReturn(emptyPage);

        // when
        Page<CommentResponse> result = commentService.getMyComments(customUser, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(commentRepository).findAllByMember(testMember, pageable);
    }

    @Test
    void 내_게시글_댓글_목록_조회_성공() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));

        List<Comment> comments = Arrays.asList(testComment);
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        when(commentRepository.findAllByArticle_Member(testMember, pageable))
            .thenReturn(commentPage);

        // when
        Page<CommentResponse> result = commentService.getComments(customUser, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent())
            .extracting("content", "author")
            .containsExactly(
                tuple("테스트댓글", "테스트닉네임")
            );

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(commentRepository).findAllByArticle_Member(testMember, pageable);
    }

    @Test
    void 내_게시글_댓글_목록_조회_실패_회원정보없음() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> commentService.getComments(customUser, pageable))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(commentRepository, never()).findAllByArticle_Member(any(), any());
    }

    @Test
    void 내_게시글_댓글_목록_조회_성공_댓글없음() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));

        Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(commentRepository.findAllByArticle_Member(testMember, pageable))
            .thenReturn(emptyPage);

        // when
        Page<CommentResponse> result = commentService.getComments(customUser, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(commentRepository).findAllByArticle_Member(testMember, pageable);
    }
}