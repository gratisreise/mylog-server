package com.mylog.service.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.exception.CReachedLimitException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.model.dto.category.CategoryCreateRequest;
import com.mylog.model.dto.category.CategoryUpdateRequest;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Category;
import com.mylog.domain.entity.Member;
import com.mylog.repository.category.CategoryRepository;
import com.mylog.api.member.MemberReader;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private MemberReader memberReader;

    @Mock
    private CategoryReader categoryReader;

    @Mock
    private CategoryRepository categoryRepository;

    private Member member;
    private CustomUser customUser;

    @BeforeEach
    void setUp() {
        member = createMember(1L, "test@test.com", "testuser");
        customUser = new CustomUser(member, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_성공() {
        // Given
        CategoryCreateRequest request = new CategoryCreateRequest("New Category");
        when(memberReader.getById(1L)).thenReturn(member);
        when(categoryRepository.countByMember(member)).thenReturn(5);
        when(categoryRepository.save(any(Category.class))).thenAnswer(i -> i.getArgument(0));

        // When
        categoryService.createCategory(request, customUser);

        // Then
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 최대 개수 도달")
    void createCategory_실패_최대_개수_도달() {
        // Given
        CategoryCreateRequest request = new CategoryCreateRequest("New Category");
        when(memberReader.getById(1L)).thenReturn(member);
        when(categoryRepository.countByMember(member)).thenReturn(20);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(request, customUser))
                .isInstanceOf(CReachedLimitException.class)
                .hasMessage("카테고리 갯수가 한도에 도달했습니다.");
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_성공() {
        // Given
        CategoryUpdateRequest request = new CategoryUpdateRequest("Updated Category");
        Category category = createCategory(member, "Original Category");
        when(categoryReader.getById(1L)).thenReturn(category);

        // When
        categoryService.updateCategory(request, 1L, customUser);

        // Then
        assertThat(category.getCategoryName()).isEqualTo("Updated Category");
        verify(categoryReader, times(1)).getById(1L);
    }

    @Test
    @DisplayName("카테고리 수정 실패 - 권한 없음")
    void updateCategory_권한_없음() {
        // Given
        CategoryUpdateRequest request = new CategoryUpdateRequest("Updated Category");
        Member anotherMember = createMember(2L, "another@test.com", "anotherUser");
        CustomUser anotherUser = new CustomUser(anotherMember, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Category category = createCategory(member, "Original Category"); // category is owned by member with ID 1

        when(categoryReader.getById(1L)).thenReturn(category);

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(request, 1L, anotherUser))
                .isInstanceOf(CUnAuthorizedException.class)
                .hasMessage("허용되지 않는 유저입니다.");
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_성공() {
        // Given
        Category category = createCategory(member, "Category to delete");
        when(categoryReader.getById(1L)).thenReturn(category);

        // When
        categoryService.deleteCategory(1L, customUser);

        // Then
        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 권한 없음")
    void deleteCategory_권한_없음() {
        // Given
        Member anotherMember = createMember(2L, "another@test.com", "anotherUser");
        CustomUser anotherUser = new CustomUser(anotherMember, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        Category category = createCategory(member, "Original Category");
        when(categoryReader.getById(1L)).thenReturn(category);

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(1L, anotherUser))
                .isInstanceOf(CUnAuthorizedException.class)
                .hasMessage("허용되지 않는 유저입니다.");
    }

    private Member createMember(long id, String email, String nickname) {
        return Member.builder()
                .id(id)
                .email(email)
                .password("password")
                .memberName("Test User")
                .nickname(nickname)
                .build();
    }

    private Category createCategory(Member member, String categoryName) {
        return Category.builder()
                .id(1L)
                .categoryName(categoryName)
                .member(member)
                .build();
    }
}
