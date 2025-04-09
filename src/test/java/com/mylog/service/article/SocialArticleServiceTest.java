package com.mylog.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;

import com.mylog.dto.article.ArticleCreateRequest;
import com.mylog.dto.article.ArticleResponse;
import com.mylog.dto.article.ArticleUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Article;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import com.mylog.service.TagService;
import java.util.Arrays;
import java.util.Collections;
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
class SocialArticleServiceTest {

    @InjectMocks
    private SocialArticleService socialArticleService;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagService tagService;

    private Member testMember;
    private Category testCategory;
    private CustomUser customUser;
    private Article testArticle;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .id(1L)
            .memberName("테스트유저")
            .nickname("테스트닉네임")
            .provider(OauthProvider.KAKAO)
            .providerId("12345")
            .build();

        testCategory = Category.builder()
            .id(1L)
            .categoryName("테스트카테고리")
            .build();

        customUser = new CustomUser(1L, Collections.emptyList());

        testArticle = Article.builder()
            .id(1L)
            .title("테스트제목")
            .content("테스트내용")
            .member(testMember)
            .category(testCategory)
            .build();
    }

    @Test
    void 게시글_생성_성공() {
        // given
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setTitle("테스트제목");
        request.setContent("테스트내용");
        request.setCategory("테스트카테고리");
        request.setTags(Arrays.asList("태그1", "태그2"));

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(Optional.of(testCategory));
        when(articleRepository.save(any(Article.class))).thenReturn(testArticle);

        // when & then
        assertThatCode(() -> socialArticleService.createArticle(request, customUser))
            .doesNotThrowAnyException();

        verify(articleRepository).save(any(Article.class));
        verify(tagService).saveTag(eq(request.getTags()), any(Article.class));
    }

    @Test
    void 내_게시글_목록_조회_성공() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> articlePage = new PageImpl<>(Collections.singletonList(testArticle));

        when(articleRepository.findAllByMemberId(anyLong(), any(Pageable.class))).thenReturn(articlePage);

        // when
        Page<ArticleResponse> result = socialArticleService.getArticles(pageable, customUser);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent())
            .hasSize(1)
            .extracting("title", "content", "author")
            .containsExactly(
                tuple("테스트제목", "테스트내용", "테스트닉네임")
            );
    }

    @Test
    void 게시글_수정_성공() {
        // given
        ArticleUpdateRequest request = new ArticleUpdateRequest();
        request.setId(1L);
        request.setTitle("수정된제목");
        request.setContent("수정된내용");
        request.setCategory("수정된카테고리");
        request.setAuthor("테스트닉네임");

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.findByNickname(anyString())).thenReturn(Optional.of(testMember));
        when(articleRepository.findById(anyLong())).thenReturn(Optional.of(testArticle));
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(Optional.of(testCategory));

        // when & then
        assertThatCode(() -> socialArticleService.updateArticle(request, customUser))
            .doesNotThrowAnyException();

        verify(articleRepository).findById(anyLong());
        verify(categoryRepository).findByCategoryName(anyString());
    }

    @Test
    void 게시글_생성_실패_회원정보없음() {
        // given
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setCategory("테스트카테고리");
        when(categoryRepository.findByCategoryName(anyString())).thenReturn(Optional.of(testCategory));
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> socialArticleService.createArticle(request, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(categoryRepository).findByCategoryName(any(String.class));
        verify(memberRepository).findById(1L);
        verify(articleRepository, never()).save(any());
        verify(tagService, never()).saveTag(any(), any());
    }

    @Test
    void 게시글_생성_실패_카테고리없음() {
        // given
        ArticleCreateRequest request = new ArticleCreateRequest();
        request.setCategory("없는카테고리");

        when(categoryRepository.findByCategoryName(request.getCategory()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> socialArticleService.createArticle(request, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(categoryRepository).findByCategoryName(request.getCategory());
        verify(memberRepository, never()).findById(any());
        verify(articleRepository, never()).save(any());
        verify(tagService, never()).saveTag(any(), any());
    }


    @Test
    void 게시글_수정_실패_권한없음() {
        // given
        ArticleUpdateRequest request = new ArticleUpdateRequest();
        request.setAuthor("다른유저");

        Member otherMember = Member.builder()
            .id(2L)
            .nickname("다른유저")
            .build();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(memberRepository.findByNickname("다른유저")).thenReturn(Optional.of(otherMember));

        // when & then
        assertThatThrownBy(() -> socialArticleService.updateArticle(request, customUser))
            .isInstanceOf(CUnAuthorizedException.class)
            .hasMessage("허용 되지 않는 유저입니다.");

        verify(memberRepository).findById(1L);
        verify(memberRepository).findByNickname("다른유저");
        verify(articleRepository, never()).findById(any());
        verify(categoryRepository, never()).findByCategoryName(any());
    }
}