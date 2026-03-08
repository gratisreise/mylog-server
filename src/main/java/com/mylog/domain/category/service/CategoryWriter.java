package com.mylog.domain.category.service;


import com.mylog.common.CommonValue;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.category.Category;
import com.mylog.domain.category.dto.CategoryCreateRequest;
import com.mylog.domain.category.dto.CategoryUpdateRequest;
import com.mylog.domain.category.repository.CategoryRepository;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.service.MemberReader;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryWriter {

    private final MemberReader memberReader;
    private final CategoryRepository categoryRepository;
    private final CategoryReader categoryReader;

    public Long createCategory(CategoryCreateRequest request, long memberId){
        Member member = memberReader.getById(memberId);
        int categorySize = categoryRepository.countByMember(member);

        if(categorySize == CommonValue.CATEGORY_LIMIT){
            throw new BusinessException(ErrorCode.CATEGORY_LIMIT_REACHED);
        }

        Category category = request.toEntity(member);

        categoryRepository.save(category);
        return category.getId();
    }

    @Async
    public void createCategory(Member member){
        Category category = Category.builder()
            .member(member)
            .categoryName(CommonValue.BASIC_CATEGORY)
            .build();
        categoryRepository.save(category);
    }

    public void updateCategory(CategoryUpdateRequest request, long categoryId, long memberId){
        Category category = categoryReader.getById(categoryId);

        if(!category.isOwnedBy(memberId)){
            throw new BusinessException(ErrorCode.CATEGORY_FORBIDDEN);
        }

        category.updateCategorName(request.categoryName());
    }

    public void deleteCategory(long categoryId, long memberId){
        Category category = categoryReader.getById(categoryId);

        if(!category.isOwnedBy(memberId)){
            throw new BusinessException(ErrorCode.CATEGORY_FORBIDDEN);
        }

        categoryRepository.deleteById(categoryId);
    }
}
