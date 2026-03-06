package com.mylog.category.service;

import com.mylog.category.entity.Category;

import com.mylog.category.repository.CategoryRepository;
import com.mylog.exception.common.CommonError;
import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
import com.mylog.response.CommonValue;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mylog.exception.common.CMissingDataException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryReader {

    private final CategoryRepository categoryRepository;

    public List<Category> getCategories(long memberId){
        return categoryRepository.findByMemberId(memberId);
    }

    public int categorySize(Member member){
        return categoryRepository.countByMember(member);
    }

    public Category getByMemberIdAndCategoryName(Long memberId, String categoryName) {
        return categoryRepository.findByMemberIdAndCategoryName(memberId, categoryName)
            .orElseThrow(() -> new CMissingDataException(CommonError.CATEGORY_IS_EMPTY));
    }

    public Category getById(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(() -> new CMissingDataException(CommonError.CATEGORY_IS_EMPTY));
    }
}
