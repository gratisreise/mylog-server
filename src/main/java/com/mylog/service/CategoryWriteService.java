package com.mylog.service;

import com.mylog.exception.CReachedLimitException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.model.dto.category.CategoryCreateRequest;
import com.mylog.model.dto.category.CategoryUpdateRequest;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Category;
import com.mylog.model.entity.Member;
import com.mylog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryWriteService {

    private final MemberReadService  memberReadService;
    private final CategoryRepository categoryRepository;
    private final CategoryReadService categoryReadService;

    private final int limit = 20;
    public static String originCategory = "없음";

    public void createCategory(CategoryCreateRequest request, CustomUser customUser){
        Member member = memberReadService.getById(customUser.getMemberId());
        int categorySize = categoryReadService.getCategorySize(member);

        if(categorySize == limit){
            throw new CReachedLimitException("카테고리 갯수가 한도에 도달했습니다.");
        }

        Category category = new Category(member, request.categoryName());

        categoryRepository.save(category);
    }

    public void createCategory(String email){
        Member member = memberReadService.getByEmail(email);
        Category category = new Category(member, originCategory);
        categoryRepository.save(category);
    }

    public void updateCategory(CategoryUpdateRequest request,Long categoryId, CustomUser customUser){
        Category category = categoryReadService.getById(categoryId);

        if(!isHaveAuth(category, customUser)){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        category.update(request);
    }

    public void deleteCategory(Long categoryId, CustomUser customUser){
        Category category = categoryReadService.getById(categoryId);

        if(!isHaveAuth(category, customUser)){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        categoryRepository.deleteById(categoryId);
    }

    private boolean isHaveAuth(Category category, CustomUser customUser) {
        long categoryMemberId = category.getMember().getId();
        long userMemberId = customUser.getMemberId();
        return categoryMemberId == userMemberId;
    }
}
