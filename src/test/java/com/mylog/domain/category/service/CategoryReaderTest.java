package com.mylog.domain.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.mylog.common.CommonValue;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.category.Category;
import com.mylog.domain.category.dto.CategoryResponse;
import com.mylog.domain.category.repository.CategoryRepository;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.MemberReader;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryReader 단위 테스트")
class CategoryReaderTest {

  @Mock private MemberReader memberReader;
  @Mock private CategoryRepository categoryRepository;

  @InjectMocks private CategoryReader categoryReader;

  private static final Long MEMBER_ID = 1L;
  private static final Long CATEGORY_ID = 100L;

  private Member member;
  private Category category;
  private Category basicCategory;

  @BeforeEach
  void setUp() {
    member = Member.builder().id(MEMBER_ID).build();
    category =
        Category.builder().id(CATEGORY_ID).member(member).categoryName("개발").build();
    basicCategory =
        Category.builder().id(CATEGORY_ID + 1).member(member).categoryName(CommonValue.BASIC_CATEGORY).build();
  }

  @Nested
  @DisplayName("getCategories - 회원 카테고리 목록 조회")
  class GetCategories {

    @Test
    @DisplayName("성공: 회원의 카테고리 목록 반환 (기본 카테고리 '없음' 제외)")
    void getCategories_Success() {
      // given
      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      given(categoryRepository.findByMember(member)).willReturn(List.of(category, basicCategory));

      // when
      List<CategoryResponse> result = categoryReader.getCategories(MEMBER_ID);

      // then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).categoryName()).isEqualTo("개발");
      then(memberReader).should().getById(MEMBER_ID);
      then(categoryRepository).should().findByMember(member);
    }

    @Test
    @DisplayName("성공: 기본 카테고리만 있으면 빈 목록 반환")
    void getCategories_OnlyBasicCategory_ReturnsEmptyList() {
      // given
      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      given(categoryRepository.findByMember(member)).willReturn(List.of(basicCategory));

      // when
      List<CategoryResponse> result = categoryReader.getCategories(MEMBER_ID);

      // then
      assertThat(result).isEmpty();
      then(memberReader).should().getById(MEMBER_ID);
      then(categoryRepository).should().findByMember(member);
    }
  }

  @Nested
  @DisplayName("getById - 카테고리 ID로 조회")
  class GetById {

    @Test
    @DisplayName("성공: 카테고리 ID로 조회 성공")
    void getById_Success() {
      // given
      given(categoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(category));

      // when
      Category result = categoryReader.getById(CATEGORY_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(CATEGORY_ID);
      assertThat(result.getCategoryName()).isEqualTo("개발");
      then(categoryRepository).should().findById(CATEGORY_ID);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID - CATEGORY_NOT_FOUND 예외 발생")
    void getById_NotFound_ThrowsException() {
      // given
      Long notFoundId = 999L;
      given(categoryRepository.findById(notFoundId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> categoryReader.getById(notFoundId))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);

      then(categoryRepository).should().findById(notFoundId);
    }
  }

  @Nested
  @DisplayName("getByMemberIdAndCategoryName - 회원ID와 이름으로 조회")
  class GetByMemberIdAndCategoryName {

    @Test
    @DisplayName("성공: 회원ID + 이름으로 조회 성공")
    void getByMemberIdAndCategoryName_Success() {
      // given
      String categoryName = "개발";
      given(categoryRepository.findByMemberIdAndCategoryName(MEMBER_ID, categoryName))
          .willReturn(Optional.of(category));

      // when
      Category result = categoryReader.getByMemberIdAndCategoryName(MEMBER_ID, categoryName);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getCategoryName()).isEqualTo(categoryName);
      then(categoryRepository).should().findByMemberIdAndCategoryName(MEMBER_ID, categoryName);
    }

    @Test
    @DisplayName("실패: 존재하지 않음 - CATEGORY_NOT_FOUND 예외 발생")
    void getByMemberIdAndCategoryName_NotFound_ThrowsException() {
      // given
      String categoryName = "존재하지않는카테고리";
      given(categoryRepository.findByMemberIdAndCategoryName(MEMBER_ID, categoryName))
          .willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> categoryReader.getByMemberIdAndCategoryName(MEMBER_ID, categoryName))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);

      then(categoryRepository).should().findByMemberIdAndCategoryName(MEMBER_ID, categoryName);
    }
  }

  @Nested
  @DisplayName("getCategory - 소유자 검증과 함께 카테고리 조회")
  class GetCategory {

    @Test
    @DisplayName("성공: 소유자가 조회하면 성공")
    void getCategory_Success() {
      // given
      given(categoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(category));

      // when
      CategoryResponse result = categoryReader.getCategory(CATEGORY_ID, MEMBER_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(CATEGORY_ID);
      assertThat(result.categoryName()).isEqualTo("개발");
      assertThat(result.memberId()).isEqualTo(MEMBER_ID);
      then(categoryRepository).should().findById(CATEGORY_ID);
    }

    @Test
    @DisplayName("실패: 소유자가 아님 - CATEGORY_FORBIDDEN 예외 발생")
    void getCategory_Forbidden_ThrowsException() {
      // given
      Long otherMemberId = 2L;
      given(categoryRepository.findById(CATEGORY_ID)).willReturn(Optional.of(category));

      // when & then
      assertThatThrownBy(() -> categoryReader.getCategory(CATEGORY_ID, otherMemberId))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.CATEGORY_FORBIDDEN);

      then(categoryRepository).should().findById(CATEGORY_ID);
    }
  }
}
