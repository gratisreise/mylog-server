package com.mylog.service.comment;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import com.mylog.dto.comment.CommentResponse;
import com.mylog.entity.Article;
import com.mylog.entity.Comment;
import com.mylog.entity.Member;
import com.mylog.repository.CommentRepository;
import java.util.Collections;
import java.util.List;
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
class CommonCommentServiceTest {

    @InjectMocks
    private CommonCommentService commonCommentService;

    @Mock
    private CommentRepository commentRepository;

    private Member testMember;
    private Article testArticle;
    private Comment testComment;
    private Comment testChildComment;
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
            .member(testMember)
            .article(testArticle)
            .build();

        testChildComment = Comment.builder()
            .id(2L)
            .content("테스트답글")
            .member(testMember)
            .article(testArticle)
            .parentId(testComment.getId())
            .build();

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void 게시글_댓글목록_조회_성공() {
        // given
        Long articleId = 1L;
        List<Comment> comments = Collections.singletonList(testComment);
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        when(commentRepository.findByArticleId(articleId, pageable))
            .thenReturn(commentPage);

        // when
        Page<CommentResponse> result = commonCommentService.getComments(articleId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent())
            .hasSize(1)
            .extracting("content", "author")
            .containsExactly(
                tuple("테스트댓글", "테스트닉네임")
            );

        verify(commentRepository).findByArticleId(articleId, pageable);
    }

    @Test
    void 게시글_댓글목록_조회_결과없음() {
        // given
        Long articleId = 1L;
        Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(commentRepository.findByArticleId(articleId, pageable))
            .thenReturn(emptyPage);

        // when
        Page<CommentResponse> result = commonCommentService.getComments(articleId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(commentRepository).findByArticleId(articleId, pageable);
    }

    @Test
    void 대댓글_목록_조회_성공() {
        // given
        Long articleId = 1L;
        Long parentId = 1L;
        List<Comment> comments = Collections.singletonList(testChildComment);
        Page<Comment> commentPage = new PageImpl<>(comments, pageable, comments.size());

        when(commentRepository.findByArticleIdAndParentId(articleId, parentId, pageable))
            .thenReturn(commentPage);

        // when
        Page<CommentResponse> result = commonCommentService.getChildComments(articleId, parentId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent())
            .hasSize(1)
            .extracting("content", "author")
            .containsExactly(
                tuple("테스트답글", "테스트닉네임")
            );

        verify(commentRepository).findByArticleIdAndParentId(articleId, parentId, pageable);
    }

    @Test
    void 대댓글_목록_조회_결과없음() {
        // given
        Long articleId = 1L;
        Long parentId = 1L;
        Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(commentRepository.findByArticleIdAndParentId(articleId, parentId, pageable))
            .thenReturn(emptyPage);

        // when
        Page<CommentResponse> result = commonCommentService.getChildComments(articleId, parentId, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(commentRepository).findByArticleIdAndParentId(articleId, parentId, pageable);
    }


}