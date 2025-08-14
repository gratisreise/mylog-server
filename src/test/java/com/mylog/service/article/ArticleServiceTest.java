package com.mylog.service.article;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.model.dto.article.ArticleCreateRequest;
import com.mylog.model.dto.article.ArticleUpdateRequest;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Article;
import com.mylog.model.entity.Category;
import com.mylog.model.entity.Member;
import com.mylog.repository.article.ArticleRepository;
import com.mylog.repository.articletag.ArticleTagRepository;
import com.mylog.service.S3Service;
import com.mylog.service.category.CategoryReader;
import com.mylog.service.member.MemberReader;
import com.mylog.service.tag.TagService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.multipart.MultipartFile;

/**
 * Comprehensive unit tests for ArticleWriteService
 * Tests all public methods including file upload scenarios, authorization validation, 
 * tag management integration, S3Service integration, and exception handling
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleService articleService;

    @Mock
    private ArticleReader articleReader;

    @Mock
    private CategoryReader categoryReader;

    @Mock
    private MemberReader memberReader;

    @Mock
    private ArticleTagRepository articleTagRepository;

    @Mock
    private TagService tagService;

    @Mock
    private S3Service s3Service;

    private CustomUser customUser;
    private MultipartFile mockFile;
    private ArticleCreateRequest createRequest;
    private ArticleUpdateRequest updateRequest;
    private Member testMember;
    private Article testArticle;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // Create test entities
        testMember = Member.builder()
            .id(1L)
            .email("test@example.com")
            .password("password123")
            .memberName("Test User")
            .nickname("testuser")
            .bio("Test bio")
            .profileImg("https://example.com/default.jpg")
            .provider(OauthProvider.LOCAL)
            .providerId("test@example.com" + OauthProvider.LOCAL)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        testCategory = Category.builder()
            .id(1L)
            .categoryName("Test Category")
            .member(testMember)
            .createdAt(LocalDateTime.now().toLocalDate())
            .updatedAt(LocalDateTime.now().toLocalDate())
            .build();

        testArticle = Article.builder()
            .id(1L)
            .title("Test Article")
            .content("This is test article content")
            .articleImg("https://example.com/article.jpg")
            .member(testMember)
            .category(testCategory)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        customUser = new CustomUser(testMember, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        
        mockFile = new MockMultipartFile(
            "file", 
            "test-image.jpg", 
            "image/jpeg", 
            "test image content".getBytes()
        );

        createRequest = new ArticleCreateRequest(
            "새 글 제목", 
            "새 글 내용", 
            "Test Category",
            List.of("tag1", "tag2")
        );

        updateRequest = new ArticleUpdateRequest(
            "수정된 제목",
            "수정된 내용",
            "Test Category",
            "testuser",
            List.of("tag1", "tag3")
        );
    }

    private Member.MemberBuilder createMemberBuilder() {
        return Member.builder()
                .email("test@example.com")
                .password("password123")
                .memberName("Test User")
                .nickname("testuser")
                .bio("Test bio")
                .profileImg("https://example.com/default.jpg")
                .provider(OauthProvider.LOCAL)
                .providerId("test@example.com" + OauthProvider.LOCAL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    private Article.ArticleBuilder createArticleBuilder() {
        return Article.builder()
                .title("Test Article")
                .content("This is test article content")
                .articleImg("https://example.com/article.jpg")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("아티클 생성 성공")
    void createArticle_성공() throws IOException {
        // Given
        String expectedImageUrl = "https://s3.amazonaws.com/bucket/uploaded-file.jpg";

        when(memberReader.getByCustomUser(customUser)).thenReturn(testMember);
        when(categoryReader.getByMemberAndCategoryName(testMember,"Test Category"))
            .thenReturn(testCategory);
        when(s3Service.upload(mockFile)).thenReturn(expectedImageUrl);
        when(articleRepository.save(any(Article.class))).thenReturn(testArticle);
        doNothing().when(tagService).saveTag(anyList(), any(Article.class));

        // When
        assertDoesNotThrow(() -> 
            articleService.createArticle(createRequest, customUser, mockFile)
        );

        // Then
        verify(categoryReader).getByMemberAndCategoryName(testMember,"Test Category");
        verify(memberReader).getByCustomUser(customUser);
        verify(s3Service).upload(mockFile);
        verify(articleRepository).save(any(Article.class));
        verify(tagService).saveTag(eq(createRequest.tags()), any(Article.class));
    }

    @Test
    @DisplayName("아티클 생성 - 카테고리 없음 예외")
    void createArticle_categoryNotFound_예외발생() throws IOException {
        // Given
        when(memberReader.getByCustomUser(customUser)).thenReturn(testMember);
        when(categoryReader.getByMemberAndCategoryName(testMember, "Test Category"))
            .thenThrow(new CMissingDataException());

        // When & Then
        assertThrows(CMissingDataException.class, () ->
            articleService.createArticle(createRequest, customUser, mockFile)
        );

        verify(memberReader).getByCustomUser(customUser);
        verify(categoryReader).getByMemberAndCategoryName(testMember, "Test Category");
        verify(s3Service, never()).upload(any());
        verify(articleRepository, never()).save(any());
        verify(tagService, never()).saveTag(any(List.class), any(Article.class));
    }

    @Test
    @DisplayName("아티클 생성 - 멤버 조회 실패 예외")
    void createArticle_memberNotFound_예외발생() throws IOException {
        // Given
        when(memberReader.getByCustomUser(customUser)).thenThrow(new CMissingDataException());

        // When & Then
        assertThrows(CMissingDataException.class, () ->
            articleService.createArticle(createRequest, customUser, mockFile)
        );

        verify(memberReader).getByCustomUser(customUser);
        verify(categoryReader, never()).getByMemberAndCategoryName(testMember, "Test Category");
        verify(s3Service, never()).upload(any());
        verify(articleRepository, never()).save(any());
        verify(tagService, never()).saveTag(any(List.class), any(Article.class));
    }


    @Test
    @DisplayName("아티클 생성 - S3 업로드 IOException 예외")
    void createArticle_s3IOException_예외발생() throws IOException {
        // Given
        when(categoryReader.getByMemberAndCategoryName(testMember, "Test Category")).thenReturn(testCategory);
        when(memberReader.getByCustomUser(customUser)).thenReturn(testMember);
        when(s3Service.upload(mockFile)).thenThrow(new IOException("S3 upload failed"));

        // When & Then
        assertThrows(IOException.class, () ->
            articleService.createArticle(createRequest, customUser, mockFile)
        );

        verify(categoryReader).getByMemberAndCategoryName(testMember, "Test Category");
        verify(memberReader).getByCustomUser(customUser);
        verify(s3Service).upload(mockFile);
        verify(articleRepository, never()).save(any());
        verify(tagService, never()).saveTag(any(List.class), any(Article.class));
    }

    @Test
    @DisplayName("아티클 생성 - 빈 태그 리스트")
    void createArticle_emptyTags_성공() throws IOException {
        // Given
        ArticleCreateRequest requestWithoutTags = new ArticleCreateRequest(
            "새 글 제목", 
            "새 글 내용", 
            "Test Category",
            List.of() // 빈 태그 리스트
        );
        String expectedImageUrl = "https://s3.amazonaws.com/bucket/uploaded-file.jpg";
        
        when(categoryReader.getByMemberAndCategoryName(testMember, "Test Category")).thenReturn(testCategory);
        when(memberReader.getByCustomUser(customUser)).thenReturn(testMember);
        when(s3Service.upload(mockFile)).thenReturn(expectedImageUrl);
        when(articleRepository.save(any(Article.class))).thenReturn(testArticle);
        doNothing().when(tagService).saveTag(anyList(), any(Article.class));

        // When
        assertDoesNotThrow(() -> 
            articleService.createArticle(requestWithoutTags, customUser, mockFile)
        );

        // Then
        verify(tagService).saveTag(eq(List.of()), any(Article.class));
    }

    @Test
    @DisplayName("아티클 수정 성공")
    void updateArticle_성공() throws IOException {
        // Given
        Long articleId = 1L;
        String expectedImageUrl = "https://s3.amazonaws.com/bucket/new-uploaded-file.jpg";
        Article existingArticle = createArticleBuilder()
            .id(articleId)
            .member(testMember)
            .category(testCategory)
            .articleImg("https://s3.amazonaws.com/bucket/very-long-path-with-exactly-ninety-three-chars-1234567890123/existing-image.jpg")
            .build();

        when(memberReader.getByNickname("testuser")).thenReturn(testMember);
        when(articleReader.getArticleById(articleId)).thenReturn(existingArticle);
        when(categoryReader.getByMemberAndCategoryName(testMember, "Test Category")).thenReturn(testCategory);
        when(s3Service.upload(mockFile)).thenReturn(expectedImageUrl);
        doNothing().when(tagService).saveTag(anyList(), any(Article.class));

        // When
        assertDoesNotThrow(() -> 
            articleService.updateArticle(updateRequest, customUser, mockFile, articleId)
        );

        // Then
        verify(memberReader).getByNickname("testuser");
        verify(articleReader).getArticleById(articleId);
        verify(categoryReader).getByMemberAndCategoryName(testMember,"Test Category");
        verify(s3Service).upload(mockFile);
        verify(tagService).saveTag(eq(updateRequest.tags()), eq(existingArticle));
        // Verify the update method was called by checking that the service completed successfully
        // The actual update method call verification is not possible without spying on the Article object
    }

    @Test
    @DisplayName("아티클 수정 - 권한 없음 예외")
    void updateArticle_unauthorized_예외발생() throws IOException {
        // Given
        Long articleId = 1L;
        Member anotherMember = createMemberBuilder()
            .id(2L)
            .nickname("anothernickname")
            .build();

        when(memberReader.getByNickname("testuser")).thenReturn(anotherMember);

        // When & Then
        assertThrows(CUnAuthorizedException.class, () ->
            articleService.updateArticle(updateRequest, customUser, mockFile, articleId)
        );

        verify(memberReader).getByNickname("testuser");
        verify(articleReader, never()).getArticleById(anyLong());
        verify(categoryReader, never()).getByMemberAndCategoryName(any(Member.class), anyString());
        verify(s3Service, never()).upload(any());
        verify(tagService, never()).saveTag(any(List.class), any(Article.class));
    }

    @Test
    @DisplayName("아티클 수정 - 같은 이미지 파일명인 경우")
    void updateArticle_sameImageFile_성공() throws IOException {
        // Given
        Long articleId = 1L;
        // Create URL with exactly 93 characters before filename to match isSame method logic
        String prefix = "https://s3.amazonaws.com/bucket/very-long-path-with-exactly-ninety-three-chars-1234567890123/";
        String existingImageUrl = prefix + "test-image.jpg";
        Article existingArticle = createArticleBuilder()
            .id(articleId)
            .member(testMember)
            .category(testCategory)
            .articleImg(existingImageUrl)
            .build();

        when(memberReader.getByNickname("testuser")).thenReturn(testMember);
        when(articleReader.getArticleById(articleId)).thenReturn(existingArticle);
        when(categoryReader.getByMemberAndCategoryName(testMember, "Test Category")).thenReturn(testCategory);
        doNothing().when(tagService).saveTag(anyList(), any(Article.class));

        // When
        assertDoesNotThrow(() -> 
            articleService.updateArticle(updateRequest, customUser, mockFile, articleId)
        );

        // Then
        verify(s3Service, never()).upload(any()); // 같은 파일명이므로 업로드 안함
        verify(tagService).saveTag(eq(updateRequest.tags()), eq(existingArticle));
        // Verify the update was called by checking service completed successfully
    }

    @Test
    @DisplayName("아티클 수정 - 아티클 조회 실패 예외")
    void updateArticle_articleNotFound_예외발생() throws IOException {
        // Given
        Long articleId = 999L;

        when(memberReader.getByNickname("testuser")).thenReturn(testMember);
        when(articleReader.getArticleById(articleId))
            .thenThrow(new CMissingDataException());

        // When & Then
        assertThrows(CMissingDataException.class, () ->
            articleService.updateArticle(updateRequest, customUser, mockFile, articleId)
        );

        verify(memberReader).getByNickname("testuser");
        verify(articleReader).getArticleById(articleId);
        verify(categoryReader, never()).getByMemberAndCategoryName(any(Member.class), anyString());
        verify(s3Service, never()).upload(any());
        verify(tagService, never()).saveTag(any(List.class), any(Article.class));
    }

    @Test
    @DisplayName("아티클 수정 - 카테고리 조회 실패 예외")
    void updateArticle_categoryNotFound_예외발생() throws IOException {
        // Given
        Long articleId = 1L;

        when(memberReader.getByNickname("testuser")).thenReturn(testMember);
        when(articleReader.getArticleById(articleId)).thenReturn(testArticle);
        when(categoryReader.getByMemberAndCategoryName(testMember, "Test Category"))
            .thenThrow(new CMissingDataException());

        // When & Then
        assertThrows(CMissingDataException.class, () ->
            articleService.updateArticle(updateRequest, customUser, mockFile, articleId)
        );

        verify(memberReader).getByNickname("testuser");
        verify(articleReader).getArticleById(articleId);
        verify(categoryReader).getByMemberAndCategoryName(testMember, "Test Category");
        verify(s3Service, never()).upload(any());
        verify(tagService, never()).saveTag(any(List.class), any(Article.class));
    }



    @Test
    @DisplayName("아티클 삭제 성공")
    void deleteArticle_성공() {
        // Given
        Long articleId = 1L;
        
        when(articleReader.getArticleById(articleId)).thenReturn(testArticle);
        when(memberReader.getByCustomUser(customUser)).thenReturn(testMember);
        doNothing().when(articleTagRepository).deleteByArticle(testArticle);
        doNothing().when(s3Service).deleteImage(testArticle.getArticleImg());
        doNothing().when(articleRepository).deleteById(articleId);

        // When
        assertDoesNotThrow(() -> 
            articleService.deleteArticle(articleId, customUser)
        );

        // Then
        verify(articleReader).getArticleById(articleId);
        verify(memberReader).getByCustomUser(customUser);
        verify(articleTagRepository).deleteByArticle(testArticle);
        verify(s3Service).deleteImage(testArticle.getArticleImg());
        verify(articleRepository).deleteById(articleId);
    }

    @Test
    @DisplayName("아티클 삭제 - 권한 없음 예외")
    void deleteArticle_unauthorized_예외발생() {
        // Given
        Long articleId = 1L;
        Member anotherMember = createMemberBuilder()
            .id(2L)
            .nickname("anothernickname")
            .build();
        Article anotherArticle = createArticleBuilder()
            .id(articleId)
            .member(anotherMember)
            .build();

        when(articleReader.getArticleById(articleId)).thenReturn(anotherArticle);
        when(memberReader.getByCustomUser(customUser)).thenReturn(testMember);

        // When & Then
        assertThrows(CUnAuthorizedException.class, () ->
            articleService.deleteArticle(articleId, customUser)
        );

        verify(articleReader).getArticleById(articleId);
        verify(memberReader).getByCustomUser(customUser);
        verify(articleTagRepository, never()).deleteByArticle(any());
        verify(s3Service, never()).deleteImage(anyString());
        verify(articleRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("아티클 삭제 - 아티클 조회 실패 예외")
    void deleteArticle_articleNotFound_예외발생() {
        // Given
        Long articleId = 999L;

        when(articleReader.getArticleById(articleId))
            .thenThrow(new CMissingDataException());

        // When & Then
        assertThrows(CMissingDataException.class, () ->
            articleService.deleteArticle(articleId, customUser)
        );

        verify(articleReader).getArticleById(articleId);
        verify(memberReader, never()).getByCustomUser(any());
        verify(articleTagRepository, never()).deleteByArticle(any());
        verify(s3Service, never()).deleteImage(anyString());
        verify(articleRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("아티클 삭제 - 멤버 조회 실패 예외")
    void deleteArticle_memberNotFound_예외발생() {
        // Given
        Long articleId = 1L;

        when(articleReader.getArticleById(articleId)).thenReturn(testArticle);
        when(memberReader.getByCustomUser(customUser))
            .thenThrow(new CMissingDataException());

        // When & Then
        assertThrows(CMissingDataException.class, () ->
            articleService.deleteArticle(articleId, customUser)
        );

        verify(articleReader).getArticleById(articleId);
        verify(memberReader).getByCustomUser(customUser);
        verify(articleTagRepository, never()).deleteByArticle(any());
        verify(s3Service, never()).deleteImage(anyString());
        verify(articleRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("파일명 비교 로직 테스트 - isSame 메서드")
    void updateArticle_isSameMethod_동일파일명_성공() throws IOException {
        // Given - URL의 93번째 인덱스부터 파일명이 일치하는 경우
        Long articleId = 1L;
        // Create a URL where the filename starts at index 93  
        String prefix = "https://s3.amazonaws.com/bucket/very-long-path-with-exactly-ninety-three-chars-1234567890123/";
        String longImageUrl = prefix + "test-image.jpg";
        Article existingArticle = createArticleBuilder()
            .id(articleId)
            .member(testMember)
            .category(testCategory)
            .articleImg(longImageUrl)
            .build();

        MockMultipartFile sameFile = new MockMultipartFile(
            "file", 
            "test-image.jpg", 
            "image/jpeg", 
            "test content".getBytes()
        );

        when(memberReader.getByNickname("testuser")).thenReturn(testMember);
        when(articleReader.getArticleById(articleId)).thenReturn(existingArticle);
        when(categoryReader.getByMemberAndCategoryName(testMember, "Test Category")).thenReturn(testCategory);
        doNothing().when(tagService).saveTag(anyList(), any(Article.class));

        // When
        assertDoesNotThrow(() -> 
            articleService.updateArticle(updateRequest, customUser, sameFile, articleId)
        );

        // Then
        verify(s3Service, never()).upload(any()); // 같은 파일이므로 업로드 하지 않음
        verify(tagService).saveTag(eq(updateRequest.tags()), eq(existingArticle));
    }

    @Test
    @DisplayName("파일명 비교 로직 테스트 - 다른 파일명")
    void updateArticle_isSameMethod_다른파일명_성공() throws IOException {
        // Given
        Long articleId = 1L;
        String longImageUrl = "https://s3.amazonaws.com/bucket/very-long-path-with-exactly-ninety-three-chars-1234567890123/different-image.jpg";
        Article existingArticle = createArticleBuilder()
            .id(articleId)
            .member(testMember)
            .category(testCategory)
            .articleImg(longImageUrl)
            .build();

        String newImageUrl = "https://s3.amazonaws.com/bucket/new-image.jpg";

        when(memberReader.getByNickname("testuser")).thenReturn(testMember);
        when(articleReader.getArticleById(articleId)).thenReturn(existingArticle);
        when(categoryReader.getByMemberAndCategoryName(testMember, "Test Category")).thenReturn(testCategory);
        when(s3Service.upload(mockFile)).thenReturn(newImageUrl);
        doNothing().when(tagService).saveTag(anyList(), any(Article.class));

        // When
        assertDoesNotThrow(() -> 
            articleService.updateArticle(updateRequest, customUser, mockFile, articleId)
        );

        // Then
        verify(s3Service).upload(mockFile); // 다른 파일이므로 새로 업로드
        verify(tagService).saveTag(eq(updateRequest.tags()), eq(existingArticle));
    }

    @Test
    @DisplayName("태그 서비스 통합 테스트")
    void createArticle_tagServiceIntegration_성공() throws IOException {
        // Given
        List<String> tags = List.of("java", "spring", "jpa");
        ArticleCreateRequest requestWithTags = new ArticleCreateRequest(
            "태그 테스트 글", 
            "태그가 있는 글 내용", 
            "Test Category",
            tags
        );
        String expectedImageUrl = "https://s3.amazonaws.com/bucket/uploaded-file.jpg";
        
        when(categoryReader.getByMemberAndCategoryName(testMember, "Test Category")).thenReturn(testCategory);
        when(memberReader.getByCustomUser(customUser)).thenReturn(testMember);
        when(s3Service.upload(mockFile)).thenReturn(expectedImageUrl);
        when(articleRepository.save(any(Article.class))).thenReturn(testArticle);
        doNothing().when(tagService).saveTag(eq(tags), any(Article.class));

        // When
        assertDoesNotThrow(() -> 
            articleService.createArticle(requestWithTags, customUser, mockFile)
        );

        // Then
        verify(tagService).saveTag(eq(tags), any(Article.class));
    }

    @Test
    @DisplayName("트랜잭션 롤백 시뮬레이션 - 태그 저장 실패")
    void createArticle_tagSaveFailed_예외발생() throws IOException {
        // Given
        String expectedImageUrl = "https://s3.amazonaws.com/bucket/uploaded-file.jpg";
        
        when(categoryReader.getByMemberAndCategoryName(testMember, "Test Category")).thenReturn(testCategory);
        when(memberReader.getByCustomUser(customUser)).thenReturn(testMember);
        when(s3Service.upload(mockFile)).thenReturn(expectedImageUrl);
        when(articleRepository.save(any(Article.class))).thenReturn(testArticle);
        doThrow(new RuntimeException("태그 저장 실패")).when(tagService).saveTag(anyList(), any(Article.class));

        // When & Then
        assertThrows(RuntimeException.class, () ->
            articleService.createArticle(createRequest, customUser, mockFile)
        );

        verify(categoryReader).getByMemberAndCategoryName(testMember, "Test Category");
        verify(memberReader).getByCustomUser(customUser);
        verify(s3Service).upload(mockFile);
        verify(articleRepository).save(any(Article.class));
        verify(tagService).saveTag(eq(createRequest.tags()), any(Article.class));
    }

    @Test
    @DisplayName("멤버 권한 검증 - 다른 사용자 글 수정 시도")
    void updateArticle_differentUser_권한검증_성공() throws IOException {
        // Given
        Long articleId = 1L;
        Member differentMember = createMemberBuilder()
            .id(3L)
            .nickname("differentuser")
            .build();

        when(memberReader.getByNickname("testuser")).thenReturn(differentMember);

        // When & Then
        CUnAuthorizedException exception = assertThrows(CUnAuthorizedException.class, () ->
            articleService.updateArticle(updateRequest, customUser, mockFile, articleId)
        );

        assertEquals("허용 되지 않는 유저입니다.", exception.getMessage());
        verify(memberReader).getByNickname("testuser");
    }

    @Test
    @DisplayName("삭제 시 권한 검증 - 다른 사용자 글 삭제 시도")
    void deleteArticle_differentUser_권한검증_성공() {
        // Given
        Long articleId = 1L;
        Member differentMember = createMemberBuilder()
            .id(3L)
            .nickname("differentuser")
            .build();
        Article differentArticle = createArticleBuilder()
            .id(articleId)
            .member(differentMember)
            .build();

        when(articleReader.getArticleById(articleId)).thenReturn(differentArticle);
        when(memberReader.getByCustomUser(customUser)).thenReturn(testMember);

        // When & Then
        CUnAuthorizedException exception = assertThrows(CUnAuthorizedException.class, () ->
            articleService.deleteArticle(articleId, customUser)
        );

        assertEquals("허용 되지 않는 유저입니다.", exception.getMessage());
        verify(articleReader).getArticleById(articleId);
        verify(memberReader).getByCustomUser(customUser);
    }
}