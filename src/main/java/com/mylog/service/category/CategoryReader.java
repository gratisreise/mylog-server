package com.mylog.service.category;

import com.mylog.common.CommonValue;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.category.CategoryResponse;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Category;
import com.mylog.model.entity.Member;
import com.mylog.repository.category.CategoryRepository;
import com.mylog.service.member.MemberReader;
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

    public Category getByMemberAndCategoryName(Member member, String categoryName) {
        return categoryRepository.findByMemberAndCategoryName(member, categoryName)
            .orElseThrow(CMissingDataException::new);
    }

    public Category getById(Long categoryId) {
        return categoryRepository.findById(categoryId)
            .orElseThrow(CMissingDataException::new);
    }
}
