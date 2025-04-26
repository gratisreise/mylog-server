package com.mylog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.dto.category.CategoryCreateRequest;
import com.mylog.dto.category.CategoryDeleteRequest;
import com.mylog.dto.category.CategoryResponse;
import com.mylog.dto.category.CategoryUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CInvalidDataException;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CReachedLimitException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private CustomUser customUser;
    private CategoryCreateRequest createRequest;
    private CategoryUpdateRequest updateRequest;
    private CategoryDeleteRequest deleteRequest;
    private Member member;
    private Category category;
    private Long memberId = 123L;
    private Long categoryId = 1L;
    private String categoryName = "TestCategory";
    private String updatedCategoryName = "UpdatedCategory";

    @BeforeEach
    void setUp() {
        // 테스트용 객체 초기화
        member = Member.builder()
            .id(memberId)
            .nickname("testUser")
            .password("testPassword")
            .provider(OauthProvider.LOCAL)
            .build();

        customUser = new CustomUser(member, List.of());
        createRequest = new CategoryCreateRequest(categoryName);
        updateRequest = new CategoryUpdateRequest(categoryId, updatedCategoryName, memberId);
        deleteRequest = new CategoryDeleteRequest(categoryId, memberId);

        category = Category.builder()
            .id(categoryId)
            .categoryName(categoryName)
            .member(member)
            .build();
    }

    @Test
    void 카테고리_생성_성공_저장완료() {
        // Given
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(categoryRepository.findByMember(member)).thenReturn(Collections.emptyList());
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);

        // When
        categoryService.createCategory(createRequest, customUser);

        // Then
        verify(memberRepository).findById(memberId);
        verify(categoryRepository).findByMember(member);
        verify(categoryRepository).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getCategoryName()).isEqualTo(categoryName);
        assertThat(savedCategory.getMember()).isEqualTo(member);
    }

    @Test
    void 카테고리_생성_멤버없음_예외발생() {
        // Given
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(createRequest, customUser))
            .isInstanceOf(CMissingDataException.class);
        verify(memberRepository).findById(memberId);
        verify(categoryRepository, never()).findByMember(any());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void 카테고리_생성_한도초과_예외발생() {
        // Given
        List<Category> categories = Arrays.asList(new Category[20]);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(categoryRepository.findByMember(member)).thenReturn(categories);

        // When & Then
        assertThatThrownBy(() -> categoryService.createCategory(createRequest, customUser))
            .isInstanceOf(CReachedLimitException.class)
            .hasMessage("카테고리 갯수가 한도에 도달했습니다.");
        verify(memberRepository).findById(memberId);
        verify(categoryRepository).findByMember(member);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void 카테고리_목록조회_성공_카테고리리스트반환() {
        // Given
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(categoryRepository.findByMember(member)).thenReturn(List.of(category));

        // When
        List<CategoryResponse> responses = categoryService.getCategories(customUser);

        // Then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(categoryId);
        assertThat(responses.get(0).getCategoryName()).isEqualTo(categoryName);
        verify(memberRepository).findById(memberId);
        verify(categoryRepository).findByMember(member);
    }

    @Test
    void 카테고리_목록조회_멤버없음_예외발생() {
        // Given
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.getCategories(customUser))
            .isInstanceOf(CMissingDataException.class);
        verify(memberRepository).findById(memberId);
        verify(categoryRepository, never()).findByMember(any());
    }

    @Test
    void 카테고리_수정_성공_업데이트완료() {
        // Given
        Category mockCategory = mock(Category.class);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mockCategory));
        doNothing().when(mockCategory).update(any(CategoryUpdateRequest.class));
        // When
        categoryService.updateCategory(updateRequest, customUser);

        // Then
        verify(categoryRepository).findById(categoryId);
        verify(mockCategory).update(updateRequest);
    }

    @Test
    void 카테고리_수정_카테고리없음_예외발생() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(updateRequest, customUser))
            .isInstanceOf(CMissingDataException.class);
        verify(categoryRepository).findById(categoryId);
        verify(mock(Category.class), never()).update(any());
    }

    @Test
    void 카테고리_수정_권한없음_예외발생() {
        // Given
        updateRequest.setMemberId(999L); // 다른 memberId

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(updateRequest, customUser))
            .isInstanceOf(CUnAuthorizedException.class)
            .hasMessage("허용되지 않는 유저입니다.");

        verify(categoryRepository, never()).findById(anyLong());
        verify(mock(Category.class), never()).update(any());
    }

    @Test
    void 카테고리_삭제_성공_삭제완료() {
        // Given
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(categoryId);

        // When
        categoryService.deleteCategory(deleteRequest, customUser);

        // Then
        verify(categoryRepository).existsById(categoryId);
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void 카테고리_삭제_카테고리없음_예외발생() {
        // Given
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(deleteRequest, customUser))
            .isInstanceOf(CInvalidDataException.class);
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void 카테고리_삭제_권한없음_예외발생() {
        // Given
        deleteRequest.setMemberId(999L); // 다른 memberId

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(deleteRequest, customUser))
            .isInstanceOf(CUnAuthorizedException.class)
            .hasMessage("허용되지 않는 유저입니다.");
        verify(categoryRepository, never()).existsById(anyLong());
        verify(categoryRepository, never()).deleteById(anyLong());
    }
}