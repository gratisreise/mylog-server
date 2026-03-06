package com.mylog.domain.category.service;

import com.mylog.common.CommonValue;
import com.mylog.common.exception.CReachedLimitException;
import com.mylog.common.exception.CUnAuthorizedException;
import com.mylog.common.security.CustomUser;
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

    public void createCategory(CategoryCreateRequest request, CustomUser customUser){
        Member member = memberReader.getById(customUser.getMemberId());
        int categorySize = categoryRepository.countByMember(member);

        if(categorySize == CommonValue.CATEGORY_LIMIT){
            throw new CReachedLimitException("카테고리 갯수가 한도에 도달했습니다.");
        }

        Category category = request.toEntity(member);

        categoryRepository.save(category);
    }

    @Async
    public void createCategory(Member member){
        Category category = Category.builder()
            .member(member)
            .categoryName(CommonValue.ORIGIN_CATEGORY)
            .build();
        categoryRepository.save(category);
    }

    public void updateCategory(CategoryUpdateRequest request,Long categoryId, CustomUser customUser){
        Category category = categoryReader.getById(categoryId);

        if(!category.isOwnedBy(customUser.getMemberId())){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        category.update(request);
    }

    public void deleteCategory(Long categoryId, CustomUser customUser){
        Category category = categoryReader.getById(categoryId);

        if(!category.isOwnedBy(customUser.getMemberId())){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        categoryRepository.deleteById(categoryId);
    }
}
