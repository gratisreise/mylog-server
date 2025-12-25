package com.mylog.service.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.api.category.CategoryReader;
import com.mylog.domain.enums.OauthProvider;
import com.mylog.common.exception.CMissingDataException;
import com.mylog.api.category.CategoryResponse;
import com.mylog.api.auth.CustomUser;
import com.mylog.domain.entity.Category;
import com.mylog.domain.entity.Member;
import com.mylog.api.category.CategoryRepository;
import com.mylog.api.member.MemberReader;
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
    private MemberReader memberReader;

    @Mock
    private CategoryRepository categoryRepository;

    private CustomUser customUser;
    private Member testMember;
    private Category testCategory;
    private Category testCategory2;
    private List<Category> testCategories;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .id(1L)
                .email("test@example.com")
                .memberName("Test User")
                .nickname("testuser")
                .password("password")
                .provider(OauthProvider.LOCAL)
                .providerId("test@example.com" + OauthProvider.LOCAL)
                .build();

        customUser = new CustomUser(testMember, Collections.emptyList());

        testCategory = Category.builder()
                .id(1L)
                .categoryName("Test Category")
                .member(testMember)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        testCategory2 = Category.builder()
                .id(2L)
                .categoryName("Test Category 2")
                .member(testMember)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        testCategories = List.of(testCategory, testCategory2);
    }

    @Test
    void getCategories_성공() {
        // Given
        when(memberReader.getByCustomUser(customUser)).thenReturn(testMember);
        when(categoryRepository.findByMember(testMember)).thenReturn(testCategories);

        // When
        List<CategoryResponse> result = categoryReader.getCategories(customUser);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).categoryName()).isEqualTo("Test Category");
        assertThat(result.get(1).categoryName()).isEqualTo("Test Category 2");

        verify(memberReader).getByCustomUser(customUser);
        verify(categoryRepository).findByMember(testMember);
    }

    @Test
    void getCategories_사용자를_찾을_수_없음() {
        // Given
        when(memberReader.getByCustomUser(customUser)).thenThrow(CMissingDataException.class);

        // When & Then
        assertThatThrownBy(() -> categoryReader.getCategories(customUser))
                .isInstanceOf(CMissingDataException.class);

        verify(memberReader).getByCustomUser(customUser);
        verify(categoryRepository, never()).findByMember(any());
    }

    @Test
    void getCategories_빈_카테고리_목록() {
        // Given
        when(memberReader.getByCustomUser(customUser)).thenReturn(testMember);
        when(categoryRepository.findByMember(testMember)).thenReturn(Collections.emptyList());

        // When
        List<CategoryResponse> result = categoryReader.getCategories(customUser);

        // Then
        assertThat(result).isEmpty();
        
        verify(memberReader).getByCustomUser(customUser);
        verify(categoryRepository).findByMember(testMember);
    }

    @Test
    void getByMemberIdAndCategoryName_성공() {
        // Given
        String categoryName = "Test Category";
        when(categoryRepository.findByMemberAndCategoryName(testMember, categoryName)).thenReturn(Optional.of(testCategory));

        // When
        Category result = categoryReader.getByMemberIdAndCategoryName(testMember, categoryName);

        // Then
        assertThat(result).isEqualTo(testCategory);
        assertThat(result.getCategoryName()).isEqualTo(categoryName);

        verify(categoryRepository).findByMemberAndCategoryName(testMember, categoryName);
    }

    @Test
    void getByMemberIdAndCategoryName_카테고리를_찾을_수_없음() {
        // Given
        String categoryName = "Non-existent Category";
        when(categoryRepository.findByMemberAndCategoryName(testMember, categoryName)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryReader.getByMemberIdAndCategoryName(testMember, categoryName))
                .isInstanceOf(CMissingDataException.class);

        verify(categoryRepository).findByMemberAndCategoryName(testMember, categoryName);
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
}