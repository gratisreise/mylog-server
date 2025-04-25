package com.mylog.service;

import com.mylog.dto.article.ArticleCreateRequest;
import com.mylog.dto.article.ArticleDeleteRequest;
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
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TagService tagService;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private ArticleService articleService;

    private CustomUser customUser;
    private Member member;
    private Category category;
    private Article article;
    private MultipartFile file;
    private ArticleCreateRequest createRequest;
    private ArticleUpdateRequest updateRequest;
    private ArticleDeleteRequest deleteRequest;
    private final String categoryName = "테스트 카테고리";
    private final Long articleId = 1L;

    @BeforeEach
    void setUp() {
        // 회원 설정
        member = Member.builder()
            .id(1L)
            .nickname("테스터")
            .password("<PASSWORD>")
            .provider(OauthProvider.LOCAL)
            .build();

        // 사용자 정보 설정
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        customUser = new CustomUser(member, Collections.singletonList(authority));

        // 카테고리 설정
        category = Category.builder()
            .id(1L)
            .categoryName(categoryName)
            .build();

        // 게시글 설정
        article = Article.builder()
            .id(1L)
            .title("테스트 제목")
            .content("테스트 내용")
            .category(category)
            .member(member)
            .articleImg("s3://mylog-imgsource/basic-e5ed3fb29dfb594e9628219c94e3d288a09797886ea8247ce512476e5cd6734e.png")
            .build();

        // 파일 설정
        file = new MockMultipartFile(
            "testImage",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        // 게시글 생성 요청 설정
        createRequest = new ArticleCreateRequest();
        createRequest.setTitle("테스트 제목");
        createRequest.setContent("테스트 내용");
        createRequest.setCategory(categoryName);
        createRequest.setTags(List.of("태그1", "태그2"));

        // 게시글 수정 요청 설정
        updateRequest = new ArticleUpdateRequest();
        updateRequest.setId(articleId);
        updateRequest.setTitle("수정된 제목");
        updateRequest.setContent("수정된 내용");
        updateRequest.setCategory(categoryName);
        updateRequest.setTags(List.of("태그1", "태그3"));

        // 게시글 삭제 요청 설정
        deleteRequest = new ArticleDeleteRequest();
        deleteRequest.setId(articleId);
        deleteRequest.setAuthor(member.getNickname());
    }

    @Test
    @DisplayName("게시글_생성_성공_테스트")
    void 게시글_생성_성공() throws IOException {
        // given
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.of(category));
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));
        when(s3Service.upload(any(MultipartFile.class))).thenReturn(Optional.of("test-image-url.jpg"));
        when(articleRepository.save(any(Article.class))).thenReturn(article);
        doNothing().when(tagService).saveTag(anyList(), any(Article.class));

        // when & then
        assertThatCode(() -> articleService.createArticle(createRequest, customUser, file))
            .doesNotThrowAnyException();

        verify(categoryRepository, times(1)).findByCategoryName(categoryName);
        verify(memberRepository, times(1)).findById(customUser.getMemberId());
        verify(s3Service, times(1)).upload(any(MultipartFile.class));
        verify(articleRepository, times(1)).save(any(Article.class));
        verify(tagService, times(1)).saveTag(anyList(), any(Article.class));
    }

    @Test
    void 게시글_생성_카테고리없음_예외발생() throws IOException {
        // given
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> articleService.createArticle(createRequest, customUser, file))
            .isInstanceOf(CMissingDataException.class);

        verify(categoryRepository, times(1)).findByCategoryName(categoryName);
        verify(memberRepository, never()).findById(anyLong());
        verify(s3Service, never()).upload(any(MultipartFile.class));
        verify(articleRepository, never()).save(any(Article.class));
        verify(tagService, never()).saveTag(anyList(), any(Article.class));
    }

    @Test
    void 게시글_생성_회원없음_예외발생() throws IOException {
        // given
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.of(category));
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> articleService.createArticle(createRequest, customUser, file))
            .isInstanceOf(CMissingDataException.class);

        verify(categoryRepository, times(1)).findByCategoryName(categoryName);
        verify(memberRepository, times(1)).findById(customUser.getMemberId());
        verify(s3Service, never()).upload(any(MultipartFile.class));
        verify(articleRepository, never()).save(any(Article.class));
        verify(tagService, never()).saveTag(anyList(), any(Article.class));
    }

    @Test
    void 게시글_생성_업로드실패_예외발생() throws IOException {
        // given
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.of(category));
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));
        when(s3Service.upload(any(MultipartFile.class))).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> articleService.createArticle(createRequest, customUser, file))
            .isInstanceOf(CMissingDataException.class);

        verify(categoryRepository, times(1)).findByCategoryName(categoryName);
        verify(memberRepository, times(1)).findById(customUser.getMemberId());
        verify(s3Service, times(1)).upload(any(MultipartFile.class));
        verify(articleRepository, never()).save(any(Article.class));
        verify(tagService, never()).saveTag(anyList(), any(Article.class));
    }

    @Test
    void 게시글_수정_성공() throws IOException {
        // given
        when(memberRepository.findByNickname(updateRequest.getAuthor())).thenReturn(Optional.of(member));
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.of(category));
        when(s3Service.upload(any(MultipartFile.class))).thenReturn(Optional.of("new-image-url.jpg"));
        doNothing().when(tagService).saveTag(anyList(), any(Article.class));

        // when
        articleService.updateArticle(updateRequest, customUser, file);

        // then
        verify(memberRepository, times(1)).findByNickname(updateRequest.getAuthor());
        verify(memberRepository, times(1)).findById(customUser.getMemberId());
        verify(articleRepository, times(1)).findById(articleId);
        verify(categoryRepository, times(1)).findByCategoryName(categoryName);
        verify(s3Service, times(1)).upload(any(MultipartFile.class));
        verify(tagService, times(1)).saveTag(anyList(), any(Article.class));

        assertThat(article.getTitle()).isEqualTo("수정된 제목");
        assertThat(article.getContent()).isEqualTo("수정된 내용");
        assertThat(article.getCategory()).isEqualTo(category);
        assertThat(article.getArticleImg()).isEqualTo("new-image-url.jpg");
    }

    @Test
    void 게시글_수정_권한없음_예외발생() throws IOException {
        // given
        Member otherMember = Member.builder()
            .id(2L)
            .nickname("otherUser")
            .password("other<PASSWORD>")
            .provider(OauthProvider.LOCAL)
            .build();
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        CustomUser otherUser = new CustomUser(otherMember, Collections.singletonList(authority));

        when(memberRepository.findById(otherUser.getMemberId())).thenReturn(Optional.of(otherMember));
        when(memberRepository.findByNickname(updateRequest.getAuthor())).thenReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> articleService.updateArticle(updateRequest, otherUser, file))
            .isInstanceOf(CUnAuthorizedException.class);

        verify(memberRepository, times(1)).findById(otherUser.getMemberId());
        verify(memberRepository, times(1)).findByNickname(updateRequest.getAuthor());
        verify(articleRepository, never()).findById(articleId);
        verify(categoryRepository, never()).findByCategoryName(anyString());
        verify(s3Service, never()).upload(any(MultipartFile.class));
        verify(tagService, never()).saveTag(anyList(), any(Article.class));
    }

    @Test
    void 게시글_삭제_성공() {
        // given
        when(memberRepository.findByNickname(deleteRequest.getAuthor())).thenReturn(Optional.of(member));
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        doNothing().when(s3Service).deleteImage(article.getArticleImg());
        doNothing().when(articleRepository).deleteById(article.getId());

        // when
        articleService.deleteArticle(deleteRequest, customUser);

        // then
        verify(memberRepository, times(1)).findByNickname(deleteRequest.getAuthor());
        verify(memberRepository, times(1)).findById(customUser.getMemberId());
        verify(articleRepository, times(1)).findById(articleId);
        verify(s3Service, times(1)).deleteImage(article.getArticleImg());
        verify(articleRepository, times(1)).deleteById(articleId);
    }

    @Test
    void 게시글_삭제_권한없음_예외발생() {
        // given
        Member otherMember = Member.builder()
            .id(2L)
            .nickname("otherUser")
            .password("other<PASSWORD>")
            .provider(OauthProvider.LOCAL)
            .build();
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        CustomUser otherUser = new CustomUser(otherMember, Collections.singletonList(authority));

        when(memberRepository.findById(otherUser.getMemberId())).thenReturn(Optional.of(otherMember));
        when(memberRepository.findByNickname(deleteRequest.getAuthor())).thenReturn(Optional.of(member));

        // when & then
        assertThatThrownBy(() -> articleService.deleteArticle(deleteRequest, otherUser))
            .isInstanceOf(CUnAuthorizedException.class);

        verify(memberRepository, times(1)).findById(otherUser.getMemberId());
        verify(memberRepository, times(1)).findByNickname(deleteRequest.getAuthor());
        verify(s3Service, never()).deleteImage(anyString());
        verify(articleRepository, never()).findById(articleId);
        verify(articleRepository, never()).deleteById(deleteRequest.getId());
    }

    @Test
    void 게시글_상세조회_성공() {
        // given
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        // when
        ArticleResponse response = articleService.getArticle(articleId);
        // then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(articleId);
        assertThat(response.getTitle()).isEqualTo("테스트 제목");
        assertThat(response.getContent()).isEqualTo("테스트 내용");
        assertThat(response.getCategory()).isEqualTo(categoryName);
        assertThat(response.getAuthor()).isEqualTo("테스터");

        verify(articleRepository, times(1)).findById(articleId);
    }

    @Test
    void 전체_게시글_목록조회_성공() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Article> articles = Arrays.asList(article);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        when(articleRepository.findAll(pageable)).thenReturn(articlePage);

        // when
        Page<ArticleResponse> responses = articleService.getArticles(pageable);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getId()).isEqualTo(articleId);
        assertThat(responses.getContent().get(0).getTitle()).isEqualTo("테스트 제목");

        verify(articleRepository, times(1)).findAll(pageable);
    }

    @Test
    void 내_게시글_목록_조회_성공() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Article> articles = Arrays.asList(article);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        when(articleRepository.findAllByMemberId(customUser.getMemberId(), pageable)).thenReturn(articlePage);
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));

        // when
        Page<ArticleResponse> responses = articleService.getArticles(pageable, customUser);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getId()).isEqualTo(articleId);
        assertThat(responses.getContent().get(0).getTitle()).isEqualTo("테스트 제목");

        verify(articleRepository, times(1)).findAllByMemberId(customUser.getMemberId(), pageable);
    }

    @Test
    void 전체_키워드_검색_성공() {
        // given
        String keyword = "테스트";
        Pageable pageable = PageRequest.of(0, 10);
        List<Article> articles = Arrays.asList(article);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        when(articleRepository.findByTitleContainingIgnoreCase(keyword, pageable)).thenReturn(articlePage);

        // when
        Page<ArticleResponse> responses = articleService.getArticles(keyword, pageable);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getId()).isEqualTo(articleId);
        assertThat(responses.getContent().get(0).getTitle()).isEqualTo("테스트 제목");

        verify(articleRepository, times(1)).findByTitleContainingIgnoreCase(keyword, pageable);
    }

    @Test
    void 내_키워드_검색_성공() {
        // given
        String keyword = "테스트";
        Pageable pageable = PageRequest.of(0, 10);
        List<Article> articles = Arrays.asList(article);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        when(articleRepository.findByMemberIdAndTitleContainingIgnoreCase(
            customUser.getMemberId(), keyword, pageable)).thenReturn(articlePage);
        when(memberRepository.findById(customUser.getMemberId())).thenReturn(Optional.of(member));

        // when
        Page<ArticleResponse> responses = articleService.getArticles(pageable, customUser, keyword);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getId()).isEqualTo(articleId);
        assertThat(responses.getContent().get(0).getTitle()).isEqualTo("테스트 제목");

        verify(memberRepository, times(1)).findById(customUser.getMemberId());
        verify(articleRepository, times(1)).findByMemberIdAndTitleContainingIgnoreCase(
            customUser.getMemberId(), keyword, pageable);
    }

    @Test
    void 태그_검색_성공() {
        // given
        String tagName = "태그1";
        Pageable pageable = PageRequest.of(0, 10);
        List<Article> articles = Arrays.asList(article);
        Page<Article> articlePage = new PageImpl<>(articles, pageable, articles.size());

        when(articleRepository.findAllByTagName(tagName, pageable)).thenReturn(articlePage);

        // when
        Page<ArticleResponse> responses = articleService.getArticlesByTagName(tagName, pageable);

        // then
        assertThat(responses).isNotNull();
        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getId()).isEqualTo(articleId);
        assertThat(responses.getContent().get(0).getTitle()).isEqualTo("테스트 제목");

        verify(articleRepository, times(1)).findAllByTagName(tagName, pageable);
    }
}