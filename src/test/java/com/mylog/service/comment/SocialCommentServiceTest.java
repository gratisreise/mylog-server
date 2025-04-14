package com.mylog.service.comment;

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
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialCommentServiceTest {

    @InjectMocks
    private SocialCommentService socialCommentService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CommonNotificationService notificationService;

    private CustomUser customUser;
    private Member member;
    private Article article;
    private Comment comment;
    private CommentCreateRequest createRequest;
    private CommentUpdateRequest updateRequest;
    private Pageable pageable;

    @BeforeEach
    void 초기화() {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        customUser = new CustomUser("1", Collections.singletonList(authority));

        member = Member.builder()
            .id(1L)
            .build();

        article = Article.builder()
            .id(1L)
            .member(member)
            .build() ;

        comment = Comment.builder()
            .id(1L)
            .member(member)
            .article(article)
            .content("테스트 댓글")
            .build();
        createRequest = new CommentCreateRequest("테스트 댓글", 1L, 0);
        updateRequest = new CommentUpdateRequest("수정된 댓글");
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void 댓글생성_유효한요청_댓글저장및알림전송() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        socialCommentService.createComment(createRequest, customUser);

        verify(commentRepository).save(any(Comment.class));
        verify(notificationService).createNotificationSetting(eq(member), eq("comment"));
        verify(notificationService).sendNotification(eq(member), eq(1L), eq("comment"));
    }

    @Test
    void 댓글생성_게시글없음_예외발생() {
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> socialCommentService.createComment(createRequest, customUser))
            .isInstanceOf(CMissingDataException.class);
    }

    @Test
    void 댓글생성_회원없음_예외발생() {
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> socialCommentService.createComment(createRequest, customUser))
            .isInstanceOf(CMissingDataException.class);
    }

    @Test
    void 댓글수정_유효한요청_댓글내용수정() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        socialCommentService.updateComment(updateRequest, 1L, customUser);

        assertThat(comment.getContent()).isEqualTo("수정된 댓글");
        verify(commentRepository).findById(1L);
    }

    @Test
    void 댓글수정_댓글없음_예외발생() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> socialCommentService.updateComment(updateRequest, 1L, customUser))
            .isInstanceOf(CMissingDataException.class);
    }

    @Test
    void 댓글수정_권한없음_예외발생() {
        Member otherMember = new Member();
        otherMember.setId(2L);
        comment.setMember(otherMember);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> socialCommentService.updateComment(updateRequest, 1L, customUser))
            .isInstanceOf(CUnAuthorizedException.class)
            .hasMessage("허용되지 않는 유저입니다.");
    }

    @Test
    void 댓글삭제_유효한요청_댓글삭제() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        socialCommentService.deleteComment(1L, customUser);

        verify(commentRepository).deleteById(1L);
    }

    @Test
    void 댓글삭제_댓글없음_예외발생() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> socialCommentService.deleteComment(1L, customUser))
            .isInstanceOf(CMissingDataException.class);
    }

    @Test
    void 댓글삭제_권한없음_예외발생() {
        Member otherMember = new Member();
        otherMember.setId(2L);
        comment.setMember(otherMember);
        Article otherArticle = new Article();
        otherArticle.setMember(otherMember);
        comment.setArticle(otherArticle);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() -> socialCommentService.deleteComment(1L, customUser))
            .isInstanceOf(CUnAuthorizedException.class)
            .hasMessage("허용되지 않는 유저입니다.");
    }

    @Test
    void 내_댓글조회_유효한요청_댓글목록반환() {
        Page<Comment> commentPage = new PageImpl<>(List.of(comment));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(commentRepository.findAllByMember(member, pageable)).thenReturn(commentPage);

        Page<CommentResponse> result = socialCommentService.getMyComments(customUser, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("테스트 댓글");
    }

    @Test
    void 내_댓글조회_회원없음_예외발생() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> socialCommentService.getMyComments(customUser, pageable))
            .isInstanceOf(CMissingDataException.class);
    }

    @Test
    void 게시글댓글조회_유효한요청_댓글목록반환() {
        Page<Comment> commentPage = new PageImpl<>(List.of(comment));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(commentRepository.findAllByArticle_Member(member, pageable)).thenReturn(commentPage);

        Page<CommentResponse> result = socialCommentService.getComments(customUser, pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getContent()).isEqualTo("테스트 댓글");
    }

    @Test
    void 게시글댓글조회_회원없음_예외발생() {
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> socialCommentService.getComments(customUser, pageable))
            .isInstanceOf(CMissingDataException.class);
    }
}