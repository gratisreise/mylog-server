package com.mylog.service.category;

import com.mylog.exception.CReachedLimitException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.model.dto.category.CategoryCreateRequest;
import com.mylog.model.dto.category.CategoryUpdateRequest;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Category;
import com.mylog.model.entity.Member;
import com.mylog.repository.category.CategoryRepository;
import com.mylog.service.member.MemberReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final MemberReader memberReader;
    private final CategoryRepository categoryRepository;
    private final CategoryReader categoryReader;

    private final int limit = 20;
    public static String originCategory = "없음";

    public void createCategory(CategoryCreateRequest request, CustomUser customUser){
        Member member = memberReader.getById(customUser.getMemberId());
        int categorySize = categoryReader.getCategorySize(member);

        if(categorySize == limit){
            throw new CReachedLimitException("카테고리 갯수가 한도에 도달했습니다.");
        }

        Category category = new Category(member, request.categoryName());

        categoryRepository.save(category);
    }

    public void createCategory(String email){
        Member member = memberReader.getByEmail(email);
        Category category = new Category(member, originCategory);
        categoryRepository.save(category);
    }

    public void createCategory(Member member){
        if(categoryRepository.existsByMemberAndCategoryName(member, CategoryService.originCategory)){
            return;
        }
        Category category = new Category(member, originCategory);
        categoryRepository.save(category);
    }

    public void updateCategory(CategoryUpdateRequest request,Long categoryId, CustomUser customUser){
        Category category = categoryReader.getById(categoryId);

        if(!isHaveAuth(category, customUser)){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        category.update(request);
    }

    public void deleteCategory(Long categoryId, CustomUser customUser){
        Category category = categoryReader.getById(categoryId);

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
