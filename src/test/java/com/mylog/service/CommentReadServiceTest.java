package com.mylog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.classes.Reply;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.comment.CommentArticleResponse;
import com.mylog.model.dto.comment.CommentResponse;
import com.mylog.model.entity.Article;
import com.mylog.model.entity.Category;
import com.mylog.model.entity.Comment;
import com.mylog.model.entity.Member;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CommentRepository;
import com.mylog.service.comment.CommentReadService;
import com.mylog.service.member.MemberReadService;
import java.time.LocalDateTime;
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
class CommentReadServiceTest {

    @InjectMocks
    private CommentReadService commentReadService;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private MemberReadService memberReadService;

    @Mock
    private ArticleRepository articleRepository;

    private CustomUser customUser;
    private Member testMember;
    private Member testMember2;
    private Article testArticle;
    private Category testCategory;
    private Comment testComment;
    private Pageable pageable;
    private Comment parentComment;
    private Comment childComment1;

    private List<Comment> comments;
    private List<Reply> replies;

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
        pageable = PageRequest.of(0, 20);
        
        // 부모 댓글 생성
        parentComment = Comment.builder()
                .id(1L)
                .content("부모 댓글")
                .article(testArticle)
                .member(testMember)
                .parentId(0L)
                .createdAt(LocalDateTime.now().minusHours(2))
                .updatedAt(LocalDateTime.now().minusHours(2))
                .build();
        
        // 자식 댓글 생성 (대댓글)
        childComment1 = Comment.builder()
                .id(2L)
                .content("자식 댓글")
                .article(testArticle)
                .member(testMember2)
                .parentId(1L) // 부모 댓글 ID
                .createdAt(LocalDateTime.now().minusHours(1))
                .updatedAt(LocalDateTime.now().minusHours(1))
                .build();

        
        comments = List.of(parentComment, childComment1);
    }

    @Test
    void getMyComments_성공() {
        // Given
        Page<Comment> commentPage = new PageImpl<>(List.of(testComment), pageable, 1);
        
        when(memberReadService.getById(1L)).thenReturn(testMember);
        when(commentRepository.findAllByMember(testMember, pageable)).thenReturn(commentPage);

        // When
        Page<CommentResponse> result = commentReadService.getMyComments(customUser, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).content()).isEqualTo("Test comment");
        assertThat(result.getTotalElements()).isEqualTo(1);
        
        verify(memberReadService).getById(1L);
        verify(commentRepository).findAllByMember(testMember, pageable);
    }

    @Test
    void getMyComments_사용자_없음() {
        // Given
        when(memberReadService.getById(1L)).thenThrow(new CMissingDataException());

        // When & Then
        assertThatThrownBy(() -> commentReadService.getMyComments(customUser, pageable))
                .isInstanceOf(CMissingDataException.class);

        verify(memberReadService).getById(1L);
        verify(commentRepository, never()).findAllByMember(any(), any());
    }

    @Test
    void getMyComments_빈_결과() {
        // Given
        Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        
        when(memberReadService.getById(1L)).thenReturn(testMember);
        when(commentRepository.findAllByMember(testMember, pageable)).thenReturn(emptyPage);

        // When
        Page<CommentResponse> result = commentReadService.getMyComments(customUser, pageable);

        // Then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
        
        verify(memberReadService).getById(1L);
        verify(commentRepository).findAllByMember(testMember, pageable);
    }

    @Test
    void getComments_내_게시글의_댓글_조회_성공() {
        // Given
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, 2);
        
        when(memberReadService.getById(1L)).thenReturn(testMember);
        when(commentRepository.findAllByArticle_Member(testMember, pageable)).thenReturn(commentPage);

        // When
        Page<CommentResponse> result = commentReadService.getComments(customUser, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        
        verify(memberReadService).getById(1L);
        verify(commentRepository).findAllByArticle_Member(testMember, pageable);
    }

    @Test
    void getComments_게시글별_댓글_조회_성공() {
        // Given
        Long articleId = 1L;
        Page<Comment> commentPage = new PageImpl<>(List.of(parentComment), pageable, 1);
        List<Comment> replies = List.of(childComment1);

        when(articleRepository.existsById(articleId)).thenReturn(true);
        when(commentRepository.findByArticle_IdAndParentId(articleId, 0L, pageable)).thenReturn(commentPage);
        when(commentRepository.findByArticle_IdAndParentId(articleId, 1L)).thenReturn(replies);
        // When
        Page<CommentArticleResponse> result = commentReadService.getComments(articleId, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).content()).isEqualTo("부모 댓글");
        assertThat(result.getContent().get(0).replies()).hasSize(1);
        assertThat(result.getContent().get(0).replies().get(0).getContent()).isEqualTo("자식 댓글");


        verify(articleRepository).existsById(articleId);
        verify(commentRepository).findByArticle_IdAndParentId(articleId, 0L, pageable);
        verify(commentRepository).findByArticle_IdAndParentId(articleId, 1L);
    }

    @Test
    void getComments_존재하지_않는_게시글() {
        // Given
        Long articleId = 999L;
        
        when(articleRepository.existsById(articleId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> commentReadService.getComments(articleId, pageable))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("존재하지 않는 게시글 입니다.");

        verify(articleRepository).existsById(articleId);
        verify(commentRepository, never()).findByArticle_Id(any(), any());
    }

    @Test
    void getChildComments_존재하지_않는_게시글() {
        // Given
        Long articleId = 999L;
        Long parentId = 1L;
        
        when(articleRepository.existsById(articleId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> commentReadService.getChildComments(articleId, parentId, pageable))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("존재하지 않는 게시글입니다.");

        verify(articleRepository).existsById(articleId);
        verify(commentRepository, never()).existsById(any());
    }

    @Test
    void getChildComments_존재하지_않는_부모_댓글() {
        // Given
        Long articleId = 1L;
        Long parentId = 999L;
        
        when(articleRepository.existsById(articleId)).thenReturn(true);
        when(commentRepository.existsById(parentId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> commentReadService.getChildComments(articleId, parentId, pageable))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("존재하지 않는 댓글입니다.");

        verify(articleRepository).existsById(articleId);
        verify(commentRepository).existsById(parentId);
        verify(commentRepository, never()).findByArticle_IdAndParentId(any(), any(), any());
    }

    @Test
    void getById_댓글_조회_성공() {
        // Given
        Long commentId = 1L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(testComment));

        // When
        Comment result = commentReadService.getById(commentId);

        // Then
        assertThat(result).isEqualTo(testComment);
        assertThat(result.getId()).isEqualTo(commentId);
        
        verify(commentRepository).findById(commentId);
    }

    @Test
    void getById_존재하지_않는_댓글() {
        // Given
        Long commentId = 999L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentReadService.getById(commentId))
                .isInstanceOf(CMissingDataException.class);

        verify(commentRepository).findById(commentId);
    }


    @Test
    void 페이징_테스트() {
        // Given
        Pageable customPageable = PageRequest.of(1, 5); // 2페이지, 5개씩
        List<Comment> pageComments = List.of(
                Comment.builder().id(6L).content("댓글 6").article(testArticle).member(testMember).parentId(0L).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build(),
                Comment.builder().id(7L).content("댓글 7").article(testArticle).member(testMember).parentId(0L).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build()
        );
        Page<Comment> commentPage = new PageImpl<>(pageComments, customPageable, 12); // 총 12개
        
        when(memberReadService.getById(1L)).thenReturn(testMember);
        when(commentRepository.findAllByMember(testMember, customPageable)).thenReturn(commentPage);

        // When
        Page<CommentResponse> result = commentReadService.getMyComments(customUser, customPageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getNumber()).isEqualTo(1); // 현재 페이지
        assertThat(result.getSize()).isEqualTo(5); // 페이지 크기
        assertThat(result.getTotalElements()).isEqualTo(12); // 총 요소 수
        assertThat(result.getTotalPages()).isEqualTo(3); // 총 페이지 수
    }

    @Test
    void 다중_사용자_댓글_구분_테스트() {
        // Given
        Member anotherMember = Member.builder()
                .id(3L)
                .email("another@example.com")
                .memberName("Another User")
                .nickname("anotheruser")
                .password("password")
                .provider(OauthProvider.LOCAL)
                .providerId("another@example.com" + OauthProvider.LOCAL)
                .build();
        
        CustomUser anotherCustomUser = new CustomUser(anotherMember, Collections.emptyList());
        
        Comment anotherComment = Comment.builder()
                .id(3L)
                .content("다른 사용자 댓글")
                .article(testArticle)
                .member(anotherMember)
                .parentId(0L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        Page<Comment> anotherCommentPage = new PageImpl<>(List.of(anotherComment), pageable, 1);
        
        when(memberReadService.getById(3L)).thenReturn(anotherMember);
        when(commentRepository.findAllByMember(anotherMember, pageable)).thenReturn(anotherCommentPage);

        // When
        Page<CommentResponse> result = commentReadService.getMyComments(anotherCustomUser, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).content()).isEqualTo("다른 사용자 댓글");
        
        verify(memberReadService).getById(3L);
        verify(commentRepository).findAllByMember(anotherMember, pageable);
    }
}