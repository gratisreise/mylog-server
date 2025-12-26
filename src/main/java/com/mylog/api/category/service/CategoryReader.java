package com.mylog.api.category.service;

import com.mylog.api.category.entity.Category;
import com.mylog.api.category.dto.CategoryResponse;
import com.mylog.api.category.repository.CategoryRepository;
import com.mylog.common.CommonValue;
import com.mylog.exception.CMissingDataException;
import com.mylog.api.auth.CustomUser;
import com.mylog.api.member.entity.Member;
import com.mylog.api.member.service.MemberReader;
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


    public List<CategoryResponse> getCategories(CustomUser customUser){
        Member member = memberReader.getByCustomUser(customUser);
        return categoryRepository.findByMember(member)
            .stream()
            .filter(this::isOriginCategory)
            .map(CategoryResponse::new)
            .toList();
    }

    private boolean isOriginCategory(Category category){
        return !category.getCategoryName().equals(CommonValue.ORIGIN_CATEGORY);
    }

    public Category getByMemberIdAndCategoryName(Long memberId, String categoryName) {
        return categoryRepository.findByMemberIdAndCategoryName(memberId, categoryName)
            .orElseThrow(CMissingDataException::new);
    }

    public Category getById(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(CMissingDataException::new);
    }
}
