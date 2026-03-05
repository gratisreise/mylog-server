package com.mylog.domain.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willDoNothing;

import com.mylog.common.CommonValue;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.category.Category;
import com.mylog.domain.category.dto.CategoryCreateRequest;
import com.mylog.domain.category.dto.CategoryUpdateRequest;
import com.mylog.domain.category.repository.CategoryRepository;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.MemberReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryWriter 단위 테스트")
class CategoryWriterTest {

  @Mock private MemberReader memberReader;
  @Mock private CategoryRepository categoryRepository;
  @Mock private CategoryReader categoryReader;

  @InjectMocks private CategoryWriter categoryWriter;

  private static final Long MEMBER_ID = 1L;
  private static final Long CATEGORY_ID = 100L;

  private Member member;
  private Category category;

  @BeforeEach
  void setUp() {
    member = Member.builder().id(MEMBER_ID).build();
    category =
        Category.builder().id(CATEGORY_ID).member(member).categoryName("개발").build();
  }

  @Nested
  @DisplayName("createCategory - 카테고리 생성")
  class CreateCategory {

    @Test
    @DisplayName("성공: 카테고리 생성 성공, ID 반환")
    void createCategory_Success() {
      // given
      CategoryCreateRequest request = new CategoryCreateRequest("새 카테고리");

      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      given(categoryRepository.countByMember(member)).willReturn(5);
      willAnswer(invocation -> {
        Category savedCategory = invocation.getArgument(0);
        savedCategory.setId(CATEGORY_ID);
        return savedCategory;
      }).given(categoryRepository).save(any(Category.class));

      // when
      Long result = categoryWriter.createCategory(request, MEMBER_ID);

      // then
      assertThat(result).isEqualTo(CATEGORY_ID);
      then(memberReader).should().getById(MEMBER_ID);
      then(categoryRepository).should().countByMember(member);
      then(categoryRepository).should().save(any(Category.class));
    }

    @Test
    @DisplayName("실패: 카테고리 개수 20개 초과 - CATEGORY_LIMIT_REACHED 예외 발생")
    void createCategory_LimitReached_ThrowsException() {
      // given
      CategoryCreateRequest request = new CategoryCreateRequest("새 카테고리");

      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      given(categoryRepository.countByMember(member)).willReturn(CommonValue.CATEGORY_LIMIT);

      // when & then
      assertThatThrownBy(() -> categoryWriter.createCategory(request, MEMBER_ID))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.CATEGORY_LIMIT_REACHED);

      then(memberReader).should().getById(MEMBER_ID);
      then(categoryRepository).should().countByMember(member);
      then(categoryRepository).shouldHaveNoMoreInteractions();
    }
  }

  @Nested
  @DisplayName("updateCategory - 카테고리 수정")
  class UpdateCategory {

    @Test
    @DisplayName("성공: 소유자가 수정하면 성공")
    void updateCategory_Success() {
      // given
      CategoryUpdateRequest request = new CategoryUpdateRequest("수정된 카테고리");

      given(categoryReader.getById(CATEGORY_ID)).willReturn(category);

      // when
      categoryWriter.updateCategory(request, CATEGORY_ID, MEMBER_ID);

      // then
      assertThat(category.getCategoryName()).isEqualTo("수정된 카테고리");
      then(categoryReader).should().getById(CATEGORY_ID);
    }

    @Test
    @DisplayName("실패: 소유자가 아님 - CATEGORY_FORBIDDEN 예외 발생")
    void updateCategory_Forbidden_ThrowsException() {
      // given
      Long otherMemberId = 2L;
      CategoryUpdateRequest request = new CategoryUpdateRequest("수정된 카테고리");

      given(categoryReader.getById(CATEGORY_ID)).willReturn(category);

      // when & then
      assertThatThrownBy(() -> categoryWriter.updateCategory(request, CATEGORY_ID, otherMemberId))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.CATEGORY_FORBIDDEN);

      then(categoryReader).should().getById(CATEGORY_ID);
    }
  }

  @Nested
  @DisplayName("deleteCategory - 카테고리 삭제")
  class DeleteCategory {

    @Test
    @DisplayName("성공: 소유자가 삭제하면 성공")
    void deleteCategory_Success() {
      // given
      given(categoryReader.getById(CATEGORY_ID)).willReturn(category);
      willDoNothing().given(categoryRepository).deleteById(CATEGORY_ID);

      // when
      categoryWriter.deleteCategory(CATEGORY_ID, MEMBER_ID);

      // then
      then(categoryReader).should().getById(CATEGORY_ID);
      then(categoryRepository).should().deleteById(CATEGORY_ID);
    }

    @Test
    @DisplayName("실패: 소유자가 아님 - CATEGORY_FORBIDDEN 예외 발생")
    void deleteCategory_Forbidden_ThrowsException() {
      // given
      Long otherMemberId = 2L;

      given(categoryReader.getById(CATEGORY_ID)).willReturn(category);

      // when & then
      assertThatThrownBy(() -> categoryWriter.deleteCategory(CATEGORY_ID, otherMemberId))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.CATEGORY_FORBIDDEN);

      then(categoryReader).should().getById(CATEGORY_ID);
      then(categoryRepository).shouldHaveNoInteractions();
    }
  }
}
