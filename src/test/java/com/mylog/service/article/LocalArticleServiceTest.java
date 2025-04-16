package com.mylog.service.article;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.*;

import com.mylog.dto.article.ArticleCreateRequest;
import com.mylog.dto.article.ArticleDeleteRequest;
import com.mylog.dto.article.ArticleResponse;
import com.mylog.dto.article.ArticleUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Article;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import com.mylog.service.S3Service;
import com.mylog.service.TagService;
import java.io.IOException;
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
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class LocalArticleServiceTest {

    @InjectMocks
    private LocalArticleService localArticleService;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagService tagService;

    @Mock
    private S3Service s3Service;

    private Member testMember;
    private Category testCategory;
    private CustomUser customUser;
    private Article testArticle;
    private ArticleCreateRequest request;
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        // 테스트용 Member 객체 생성
        testMember = Member.builder()
            .id(1L)
            .email("test@email.com")
            .password("testPassword")
            .memberName("테스트유저")
            .nickname("테스트닉네임")
            .build();

        // 테스트용 Category 객체 생성
        testCategory = Category.builder()
            .id(1L)
            .categoryName("테스트카테고리")
            .build();

        // 테스트용 CustomUser 객체 생성
        customUser = new CustomUser(testMember, Collections.emptyList());

        // 테스트용 Article 객체 생성
        testArticle = Article.builder()
            .id(1L)
            .title("테스트제목")
            .content("테스트내용")
            .member(testMember)
            .category(testCategory)
            .build();

        // 테스트용 ArticleCreateRequest 객체 생성
        request = new ArticleCreateRequest();
        request.setTitle("테스트제목");
        request.setContent("테스트내용");
        request.setCategory("테스트카테고리");
        request.setTags(Arrays.asList("태그1", "태그2"));

        //테스트용 multipart
        file = mock(MultipartFile.class);
    }

    @Test
    void 게시글_생성_성공() throws IOException {
        // given
        mockSuccessfulDependencies();

        // when & then
        assertThatCode(() -> localArticleService.createArticle(request, customUser, file))
            .doesNotThrowAnyException();

        verify(categoryRepository).findByCategoryName(request.getCategory());
        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(articleRepository).save(any(Article.class));
        verify(tagService).saveTag(eq(request.getTags()), any(Article.class));
        verify(s3Service).upload(eq(file));
    }

    @Test
    void 게시글_생성_실패_카테고리없음() throws IOException {
        // given
        mockCategoryNotFound();

        // when & then
        assertThatThrownBy(() -> localArticleService.createArticle(request, customUser, file))
            .isInstanceOf(CMissingDataException.class);

        verify(categoryRepository).findByCategoryName(request.getCategory());
        verify(memberRepository, never()).findByEmail(any());
        verify(articleRepository, never()).save(any());
        verify(tagService, never()).saveTag(any(), any());
        verify(s3Service, never()).upload(any());
    }

    @Test
    void 게시글_생성_실패_회원정보없음() throws IOException {
        // given
        when(categoryRepository.findByCategoryName(request.getCategory()))
            .thenReturn(Optional.of(testCategory));
        mockMemberNotFound();

        // when & then
        assertThatThrownBy(() -> localArticleService.createArticle(request, customUser, file))
            .isInstanceOf(CMissingDataException.class);

        verify(categoryRepository).findByCategoryName(request.getCategory());
        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(articleRepository, never()).save(any());
        verify(s3Service, never()).upload(any());
        verify(tagService, never()).saveTag(any(), any());
    }



    @Test
    void 게시글_삭제_성공() {
        // given
        ArticleDeleteRequest request = new ArticleDeleteRequest();
        request.setId(1L);
        request.setAuthor("테스트닉네임");

        when(memberRepository.findByNickname(request.getAuthor()))
            .thenReturn(Optional.of(testMember));
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));

        // when & then
        assertThatCode(() -> localArticleService.deleteArticle(request, customUser))
            .doesNotThrowAnyException();

        verify(memberRepository).findByNickname(request.getAuthor());
        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(articleRepository).deleteById(request.getId());
    }

    @Test
    void 게시글_삭제_실패_작성자정보없음() {
        // given
        ArticleDeleteRequest request = new ArticleDeleteRequest();
        request.setAuthor("존재하지않는사용자");

        when(memberRepository.findByNickname(request.getAuthor()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> localArticleService.deleteArticle(request, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findByNickname(request.getAuthor());
        verify(memberRepository, never()).findByEmail(any());
        verify(articleRepository, never()).deleteById(any());
    }

    @Test
    void 게시글_삭제_실패_사용자정보없음() {
        // given
        ArticleDeleteRequest request = new ArticleDeleteRequest();
        request.setAuthor("테스트닉네임");

        when(memberRepository.findByNickname(request.getAuthor()))
            .thenReturn(Optional.of(testMember));
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> localArticleService.deleteArticle(request, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findByNickname(request.getAuthor());
        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(articleRepository, never()).deleteById(any());
    }

    @Test
    void 게시글_삭제_실패_권한없음() {
        // given
        ArticleDeleteRequest request = new ArticleDeleteRequest();
        request.setAuthor("다른사용자");

        Member otherMember = Member.builder()
            .id(2L)
            .email("other@email.com")
            .nickname("다른사용자")
            .build();

        when(memberRepository.findByNickname(request.getAuthor()))
            .thenReturn(Optional.of(otherMember));
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));

        // when & then
        assertThatThrownBy(() -> localArticleService.deleteArticle(request, customUser))
            .isInstanceOf(CUnAuthorizedException.class)
            .hasMessage("허용 되지 않는 유저입니다.");

        verify(memberRepository).findByNickname(request.getAuthor());
        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(articleRepository, never()).deleteById(any());
    }

    @Test
    void 내_게시글_목록_조회_성공() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Article> articles = Arrays.asList(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));
        when(articleRepository.findAllByMemberId(testMember.getId(), pageable))
            .thenReturn(articlePage);

        // when
        Page<ArticleResponse> result = localArticleService.getArticles(pageable, customUser);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent())
            .hasSize(1)
            .extracting("title", "content", "author")
            .containsExactly(tuple("테스트제목", "테스트내용", "테스트닉네임"));

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(articleRepository).findAllByMemberId(testMember.getId(), pageable);
    }

    @Test
    void 내_게시글_목록_조회_실패_회원정보없음() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> localArticleService.getArticles(pageable, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(articleRepository, never()).findAllByMemberId(any(), any());
    }

    @Test
    void 내_게시글_키워드검색_성공() {
        // given
        String keyword = "테스트";
        Pageable pageable = PageRequest.of(0, 10);
        List<Article> articles = Arrays.asList(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));
        when(articleRepository.findByMemberIdAndTitleContainingIgnoreCase(testMember.getId(), keyword, pageable))
            .thenReturn(articlePage);

        // when
        Page<ArticleResponse> result = localArticleService.getArticles(pageable, customUser, keyword);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent())
            .hasSize(1)
            .extracting("title", "content", "author")
            .containsExactly(tuple("테스트제목", "테스트내용", "테스트닉네임"));

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(articleRepository).findByMemberIdAndTitleContainingIgnoreCase(testMember.getId(), keyword, pageable);
    }

    @Test
    void 내_게시글_키워드검색_실패_회원정보없음() {
        // given
        String keyword = "테스트";
        Pageable pageable = PageRequest.of(0, 10);

        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> localArticleService.getArticles(pageable, customUser, keyword))
            .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(articleRepository, never()).findByMemberIdAndTitleContainingIgnoreCase(any(), any(), any());
    }

    @Test
    void 내_게시글_키워드검색_결과없음() {
        // given
        String keyword = "존재하지않는키워드";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));
        when(articleRepository.findByMemberIdAndTitleContainingIgnoreCase(testMember.getId(), keyword, pageable))
            .thenReturn(emptyPage);

        // when
        Page<ArticleResponse> result = localArticleService.getArticles(pageable, customUser, keyword);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(articleRepository).findByMemberIdAndTitleContainingIgnoreCase(testMember.getId(), keyword, pageable);
    }

    @Test
    void 내_게시글_키워드검색_빈문자열() {
        // given
        String keyword = "";
        Pageable pageable = PageRequest.of(0, 10);
        List<Article> articles = Arrays.asList(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));
        when(articleRepository.findByMemberIdAndTitleContainingIgnoreCase(testMember.getId(), keyword, pageable))
            .thenReturn(articlePage);

        // when
        Page<ArticleResponse> result = localArticleService.getArticles(pageable, customUser, keyword);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);

        verify(memberRepository).findByEmail(customUser.getUsername());
        verify(articleRepository).findByMemberIdAndTitleContainingIgnoreCase(testMember.getId(), keyword, pageable);
    }



    // 공통으로 사용되는 모킹 메서드
    private void mockSuccessfulDependencies() {
        when(categoryRepository.findByCategoryName(request.getCategory()))
            .thenReturn(Optional.of(testCategory));
        when(memberRepository.findByEmail(customUser.getUsername()))
            .thenReturn(Optional.of(testMember));
        when(articleRepository.save(any(Article.class)))
            .thenReturn(testArticle);
    }

    // 카테고리 없음 모킹
    private void mockCategoryNotFound() {
        when(categoryRepository.findByCategoryName(any()))
            .thenReturn(Optional.empty());
    }

    // 회원 없음 모킹
    private void mockMemberNotFound() {
        when(memberRepository.findByEmail(any()))
            .thenReturn(Optional.empty());
    }
}