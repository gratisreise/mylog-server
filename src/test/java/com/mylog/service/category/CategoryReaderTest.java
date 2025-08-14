package com.mylog.service.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.category.CategoryResponse;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Category;
import com.mylog.model.entity.Member;
import com.mylog.repository.category.CategoryRepository;
import com.mylog.repository.member.MemberRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryReaderTest {

    @InjectMocks
    private CategoryReader categoryReader;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private CustomUser customUser;
    private Member testMember;
    private Category testCategory;
    private Category testCategory2;
    private List<Category> testCategories;

    @BeforeEach
    void setUp() {
        // Create test member
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .memberName("Test User")
                .nickname("testuser")
                .password("password")
                .provider(OauthProvider.LOCAL)
                .providerId("test@example.com" + OauthProvider.LOCAL)
                .build();

        // Create CustomUser for testing
        customUser = new CustomUser(testMember, Collections.emptyList());
        
        // Create test category
        testCategory = Category.builder()
                .id(1L)
                .categoryName("Test Category")
                .member(testMember)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        
        // Create additional test category
        testCategory2 = Category.builder()
                .id(2L)
                .categoryName("Test Category 2")
                .member(testMember)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        
        // Create list of test categories
        testCategories = List.of(testCategory, testCategory2);
    }

    @Test
    void getCategories_성공() {
        // Given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(categoryRepository.findByMember(testMember)).thenReturn(testCategories);

        // When
        List<CategoryResponse> result = categoryReader.getCategories(customUser);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).categoryName()).isEqualTo("Test Category");
        assertThat(result.get(1).categoryName()).isEqualTo("Test Category 2");

        verify(memberRepository).findById(1L);
        verify(categoryRepository).findByMember(testMember);
    }

    @Test
    void getCategories_사용자를_찾을_수_없음() {
        // Given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryReader.getCategories(customUser))
                .isInstanceOf(CMissingDataException.class);

        verify(memberRepository).findById(1L);
        verify(categoryRepository, never()).findByMember(any());
    }

    @Test
    void getCategories_빈_카테고리_목록() {
        // Given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(categoryRepository.findByMember(testMember)).thenReturn(Collections.emptyList());

        // When
        List<CategoryResponse> result = categoryReader.getCategories(customUser);

        // Then
        assertThat(result).isEmpty();
        
        verify(memberRepository).findById(1L);
        verify(categoryRepository).findByMember(testMember);
    }

    @Test
    void getByMemberAndCategoryName_성공() {
        // Given
        String categoryName = "Test Category";
        when(categoryRepository.findByMemberAndCategoryName(testMember, categoryName)).thenReturn(Optional.of(testCategory));

        // When
        Category result = categoryReader.getByMemberAndCategoryName(testMember, categoryName);

        // Then
        assertThat(result).isEqualTo(testCategory);
        assertThat(result.getCategoryName()).isEqualTo(categoryName);

        verify(categoryRepository).findByMemberAndCategoryName(testMember, categoryName);
    }

    @Test
    void getByMemberAndCategoryName_카테고리를_찾을_수_없음() {
        // Given
        String categoryName = "Non-existent Category";
        when(categoryRepository.findByMemberAndCategoryName(testMember, categoryName)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryReader.getByMemberAndCategoryName(testMember, categoryName))
                .isInstanceOf(CMissingDataException.class);

        verify(categoryRepository).findByMemberAndCategoryName(testMember, categoryName);
    }

    @Test
    void getCategorySize_성공() {
        // Given
        when(categoryRepository.findByMember(testMember)).thenReturn(testCategories);

        // When
        int result = categoryRepository.countByMember(testMember);

        // Then
        assertThat(result).isEqualTo(2);

        verify(categoryRepository).findByMember(testMember);
    }

    @Test
    void getCategorySize_빈_카테고리_목록() {
        // Given
        when(categoryRepository.findByMember(testMember)).thenReturn(Collections.emptyList());

        // When
        int result = categoryRepository.countByMember(testMember);

        // Then
        assertThat(result).isEqualTo(0);

        verify(categoryRepository).findByMember(testMember);
    }

    @Test
    void getCategorySize_대량_카테고리() {
        // Given
        List<Category> manyCategories = createManyTestCategories(testMember, 15);
        when(categoryRepository.findByMember(testMember)).thenReturn(manyCategories);

        // When
        int result = categoryRepository.countByMember(testMember);

        // Then
        assertThat(result).isEqualTo(15);

        verify(categoryRepository).findByMember(testMember);
    }

    @Test
    void getById_성공() {
        // Given
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));

        // When
        Category result = categoryReader.getById(categoryId);

        // Then
        assertThat(result).isEqualTo(testCategory);
        assertThat(result.getId()).isEqualTo(categoryId);

        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void getById_카테고리를_찾을_수_없음() {
        // Given
        Long categoryId = 999L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryReader.getById(categoryId))
                .isInstanceOf(CMissingDataException.class);

        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void getById_유효하지_않은_ID() {
        // Given
        Long invalidId = null;

        // When & Then
        assertThatThrownBy(() -> categoryReader.getById(invalidId))
                .isInstanceOf(CMissingDataException.class);

        verify(categoryRepository).findById(null);
    }

    @Test
    void generateMember_성공() {
        // Given
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));

        // When
        List<CategoryResponse> result = categoryReader.getCategories(customUser);

        // Then - 내부 generateMember 메서드가 성공적으로 호출됨을 간접적으로 검증
        verify(memberRepository).findById(1L);
        verify(categoryRepository).findByMember(testMember);
    }

    @Test
    void 카테고리_정렬_테스트() {
        // Given - 생성 날짜가 다른 카테고리들
        Category oldCategory = Category.builder()
                .id(3L)
                .categoryName("Old Category")
                .member(testMember)
                .createdAt(LocalDate.now().minusDays(5))
                .updatedAt(LocalDate.now().minusDays(5))
                .build();
        
        Category recentCategory = Category.builder()
                .id(4L)
                .categoryName("Recent Category")
                .member(testMember)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();
        
        List<Category> sortedCategories = List.of(oldCategory, recentCategory);
        
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(categoryRepository.findByMember(testMember)).thenReturn(sortedCategories);

        // When
        List<CategoryResponse> result = categoryReader.getCategories(customUser);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).categoryName()).isEqualTo("Old Category");
        assertThat(result.get(1).categoryName()).isEqualTo("Recent Category");
    }

    @Test
    void 다른_사용자의_카테고리는_조회되지_않음() {
        // Given
        Member otherMember = Member.builder()
                .id(3L)
                .email("other@example.com")
                .memberName("Other User")
                .nickname("otheruser")
                .password("password")
                .provider(OauthProvider.LOCAL)
                .providerId("other@example.com" + OauthProvider.LOCAL)
                .build();
        
        CustomUser otherCustomUser = new CustomUser(otherMember, Collections.emptyList());
        
        when(memberRepository.findById(3L)).thenReturn(Optional.of(otherMember));
        when(categoryRepository.findByMember(otherMember)).thenReturn(Collections.emptyList());

        // When
        List<CategoryResponse> result = categoryReader.getCategories(otherCustomUser);

        // Then
        assertThat(result).isEmpty();
        
        verify(memberRepository).findById(3L);
        verify(categoryRepository).findByMember(otherMember);
        verify(categoryRepository, never()).findByMember(testMember);
    }

    private List<Category> createManyTestCategories(Member member, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> Category.builder()
                        .id((long) (i + 1))
                        .categoryName("Category " + i)
                        .member(member)
                        .createdAt(LocalDate.now())
                        .updatedAt(LocalDate.now())
                        .build())
                .toList();
    }
}