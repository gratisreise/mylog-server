package com.mylog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.dto.category.CategoryCreateRequest;
import com.mylog.dto.category.CategoryResponse;
import com.mylog.dto.category.CategoryUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CReachedLimitException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import java.util.ArrayList;
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
    private CustomUser otherUser;
    private CategoryCreateRequest createRequest;
    private CategoryUpdateRequest updateRequest;
    private Member member;
    private Member otherMember;
    private Category category;
    private Long memberId = 123L;
    private Long otherMemberId = 456L;
    private Long categoryId = 1L;
    private Long otherCategoryId = 2L;
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

        otherMember = Member.builder()
            .id(otherMemberId)
            .nickname("otherUser")
            .password("testPassword")
            .provider(OauthProvider.LOCAL)
            .build();

        customUser = new CustomUser(member, List.of());
        otherUser = new CustomUser(otherMember, List.of());
        createRequest = new CategoryCreateRequest(categoryName);
        updateRequest = new CategoryUpdateRequest(updatedCategoryName);

        category = Category.builder()
            .id(categoryId)
            .categoryName(categoryName)
            .member(member)
            .build();
    }

    // --- 카테고리 생성 ---
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
        assertThat(savedCategory.getMember().getId()).isEqualTo(memberId);
    }

    @Test
    void 카테고리_생성_회원정보_없음_예외발생() {
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
        List<Category> categories = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            categories.add(new Category());
        }
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
    void 기본_카테고리_생성_성공() {
        // Given
        String email = "test@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);

        // When
        categoryService.createCategory(email);

        // Then
        verify(memberRepository).findByEmail(email);
        verify(categoryRepository).save(categoryCaptor.capture());
        Category savedCategory = categoryCaptor.getValue();
        assertThat(savedCategory.getCategoryName()).isEqualTo(CategoryService.originCategory);
        assertThat(savedCategory.getMember()).isEqualTo(member);
    }

    // --- 카테고리 목록 조회 ---
    @Test
    void 카테고리_목록조회_성공() {
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

    // --- 카테고리 수정 ---
    @Test
    void 카테고리_수정_성공() {
        // Given
        Category spiedCategory = spy(category);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(spiedCategory));

        // When
        categoryService.updateCategory(updateRequest, categoryId, customUser);

        // Then
        verify(categoryRepository).findById(categoryId);
        verify(spiedCategory).update(updateRequest);
        assertThat(spiedCategory.getCategoryName()).isEqualTo(updatedCategoryName);
    }

    @Test
    void 카테고리_수정_카테고리없음_예외발생() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(updateRequest, categoryId, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(categoryRepository).findById(categoryId);
    }

    @Test
    void 카테고리_수정_권한없음_예외발생() {
        // Given
        Category otherCategory = Category.builder()
            .id(otherCategoryId)
            .categoryName("OtherCategory")
            .member(otherMember)
            .build();
        when(categoryRepository.findById(otherCategoryId)).thenReturn(Optional.of(otherCategory));

        // When & Then
        assertThatThrownBy(() -> categoryService.updateCategory(updateRequest, otherCategoryId, customUser))
            .isInstanceOf(CUnAuthorizedException.class);

        verify(categoryRepository).findById(otherCategoryId);
    }

    // --- 카테고리 삭제 ---
    @Test
    void 카테고리_삭제_성공() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).deleteById(anyLong());

        // When
        categoryService.deleteCategory(categoryId, customUser);

        // Then
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void 카테고리_삭제_권한없음_예외발생() {
        // Given
        Category otherCategory = Category.builder()
            .id(otherCategoryId)
            .categoryName("OtherCategory")
            .member(otherMember)
            .build();
        when(categoryRepository.findById(otherCategoryId)).thenReturn(Optional.of(otherCategory));

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(otherCategoryId, customUser))
            .isInstanceOf(CUnAuthorizedException.class);

        verify(categoryRepository).findById(otherCategoryId);
        verify(categoryRepository, never()).existsById(anyLong());
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void 카테고리_삭제_카테고리없음_예외발생() {
        // Given
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId, customUser))
            .isInstanceOf(CMissingDataException.class);

        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository, never()).deleteById(anyLong());
    }
}