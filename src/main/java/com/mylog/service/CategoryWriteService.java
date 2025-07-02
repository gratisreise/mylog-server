package com.mylog.service;

import com.mylog.model.dto.category.CategoryCreateRequest;
import com.mylog.model.dto.category.CategoryUpdateRequest;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Category;
import com.mylog.model.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CReachedLimitException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryWriteService {

    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    private final int limit = 20;
    public static String originCategory = "없음";

    public void createCategory(CategoryCreateRequest request, CustomUser customUser){
        Member member = generateMember(customUser);
        int categorySize = categoryRepository.findByMember(member).size();
        if(categorySize == limit){
            throw new CReachedLimitException("카테고리 갯수가 한도에 도달했습니다.");
        }
        Category category = Category.builder()
            .member(member)
            .categoryName(request.getCategoryName())
            .build();

        categoryRepository.save(category);
    }

    public void createCategory(String email){
        Member member = memberRepository.findByEmail(email).orElseThrow(CMissingDataException::new);

        Category category = Category.builder()
            .member(member)
            .categoryName(originCategory)
            .build();
        categoryRepository.save(category);
    }

    public void updateCategory(CategoryUpdateRequest request,Long categoryId, CustomUser customUser){
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(CMissingDataException::new);

        if(!isHaveAuth(category, customUser)){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        category.update(request);
    }

    public void deleteCategory(Long categoryId, CustomUser customUser){
        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(CMissingDataException::new);

        if(!isHaveAuth(category, customUser)){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        }

        categoryRepository.deleteById(categoryId);
    }


    private Member generateMember(CustomUser customUser) {
        return memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
    }

    private boolean isHaveAuth(Category category, CustomUser customUser) {
        long categoryMemberId = category.getMember().getId();
        long userMemberId = customUser.getMemberId();
        return categoryMemberId == userMemberId;
    }
}
