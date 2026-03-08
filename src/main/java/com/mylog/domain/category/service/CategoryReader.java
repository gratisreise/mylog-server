package com.mylog.domain.category.service;


import com.mylog.common.CommonValue;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.category.Category;
import com.mylog.domain.category.dto.CategoryResponse;
import com.mylog.domain.category.repository.CategoryRepository;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.service.MemberReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryReader {

    private final MemberReader memberReader;
    private final CategoryRepository categoryRepository;


    public List<CategoryResponse> getCategories(long memberId){
        Member member = memberReader.getById(memberId);
        return categoryRepository.findByMember(member)
            .stream()
            .filter(this::isOriginCategory)
            .map(CategoryResponse::new)
            .toList();
    }

    private boolean isOriginCategory(Category category){
        return !category.getCategoryName().equals(CommonValue.BASIC_CATEGORY);
    }

    public Category getByMemberIdAndCategoryName(Long memberId, String categoryName) {
        return categoryRepository.findByMemberIdAndCategoryName(memberId, categoryName)
            .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    public Category getById(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    public CategoryResponse getCategory(Long categoryId, Long memberId) {
        Category category = getById(categoryId);
        if (!category.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.CATEGORY_FORBIDDEN);
        }
        return new CategoryResponse(category);
    }
}
