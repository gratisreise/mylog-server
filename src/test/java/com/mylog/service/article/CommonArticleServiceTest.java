package com.mylog.service.article;

import com.mylog.dto.article.ArticleCreateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Article;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.ArticleRepository;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import com.mylog.service.S3Service;
import com.mylog.service.TagService;
import com.mylog.service.article.ArticleService;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommonArticleServiceTest {

    @InjectMocks
    private CommonArticleService articleService;

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

    private ArticleCreateRequest request;
    private CustomUser customUser;
    private MockMultipartFile file;
    private Category category;
    private Member member;
    private Article article;
    private String imageUrl = "https://s3.example.com/image.jpg";
    private Long memberId = 123L;
    private String categoryName = "TestCategory";
    private String title = "Test Title";
    private String content = "Test Content";
    private List<String> tags = List.of("tag1", "tag2");

    @BeforeEach
    void setUp() {
        // 테스트용 객체 초기화
        request = new ArticleCreateRequest();
        request.setCategory(categoryName);
        request.setTitle(title);
        request.setContent(content);
        request.setTags(tags);

        member = new Member();
        member.setId(memberId);
        member.setNickname("testUser");
        member.setPassword("<PASSWORD>");
        member.setProvider(OauthProvider.LOCAL);

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_USER");
        customUser = new CustomUser(member, Collections.singletonList(authority));

        file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test image content".getBytes());

        category = new Category();
        category.setCategoryName(categoryName);

        article = Article.builder()
            .title(title)
            .content(content)
            .category(category)
            .member(member)
            .articleImg(imageUrl)
            .build();
    }

    @Test
    void 게시글_생성_성공_저장및태그처리() throws IOException {
        // Given
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.of(category));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(s3Service.upload(file)).thenReturn(Optional.of(imageUrl));
        when(articleRepository.save(any(Article.class))).thenReturn(article);
        doNothing().when(tagService).saveTag(tags, article);

        // When
        articleService.createArticle(request, customUser, file);

        // Then
        verify(categoryRepository).findByCategoryName(categoryName);
        verify(memberRepository).findById(memberId);
        verify(s3Service).upload(file);
        verify(articleRepository).save(any(Article.class));
        verify(tagService).saveTag(tags, article);
        assertThat(article.getTitle()).isEqualTo(title);
        assertThat(article.getContent()).isEqualTo(content);
        assertThat(article.getCategory()).isEqualTo(category);
        assertThat(article.getMember()).isEqualTo(member);
        assertThat(article.getArticleImg()).isEqualTo(imageUrl);
    }

    @Test
    void 게시글_생성_카테고리없음_예외발생() throws IOException {
        // Given
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> articleService.createArticle(request, customUser, file))
            .isInstanceOf(CMissingDataException.class);
        verify(categoryRepository).findByCategoryName(categoryName);
        verify(memberRepository, never()).findById(anyLong());
        verify(s3Service, never()).upload(any());
        verify(articleRepository, never()).save(any());
        verify(tagService, never()).saveTag(any(), any());
    }


    @Test
    void 게시글_생성_S3업로드실패_예외발생() throws IOException {
        // Given
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.of(category));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(s3Service.upload(file)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> articleService.createArticle(request, customUser, file))
            .isInstanceOf(CMissingDataException.class);
        verify(categoryRepository).findByCategoryName(categoryName);
        verify(memberRepository).findById(memberId);
        verify(s3Service).upload(file);
        verify(articleRepository, never()).save(any());
        verify(tagService, never()).saveTag(any(), any());
    }
//
    @Test
    void 게시글_생성_IOException발생_예외전파() throws IOException {
        // Given
        when(categoryRepository.findByCategoryName(categoryName)).thenReturn(Optional.of(category));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(s3Service.upload(file)).thenThrow(new IOException("S3 upload failed"));

        // When & Then
        assertThatThrownBy(() -> articleService.createArticle(request, customUser, file))
            .isInstanceOf(IOException.class)
            .hasMessage("S3 upload failed");
        verify(categoryRepository).findByCategoryName(categoryName);
        verify(memberRepository).findById(memberId);
        verify(s3Service).upload(file);
        verify(articleRepository, never()).save(any());
        verify(tagService, never()).saveTag(any(), any());
    }
}