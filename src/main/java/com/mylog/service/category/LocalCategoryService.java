package com.mylog.service.category;

import com.mylog.annotations.ServiceType;
import com.mylog.dto.category.CategoryCreateRequest;
import com.mylog.dto.category.CategoryResponse;
import com.mylog.dto.category.CategoryUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ServiceType(OauthProvider.LOCAL)
@Transactional(readOnly = true)
public class LocalCategoryService implements CategoryService {
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void createCategory(CategoryCreateRequest request, CustomUser customUser) {
        //사용자 가져오기
        Member member = generateMember(customUser);

        //카테고리 객체 생성
        Category category = Category.builder()
            .member(member)
            .categoryName(request.getCategoryName())
            .build();

        //저장
        categoryRepository.save(category);
    }


    @Override
    @Transactional
    public void updateCategory(CategoryUpdateRequest request, CustomUser customUser) {
        //유저 검증
        if(!validateUpdate(request, customUser)){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        };

        //카테고리 수정
        categoryRepository.findById(request.getId())
            .orElseThrow(CMissingDataException::new)
            .update(request);
    }


    @Override
    @Transactional
    public void deleteCategory(CustomUser customUser, Long categoryId) {
        //유저 검증
        if(!validateDelete(customUser, categoryId)){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        };

        categoryRepository.deleteById(categoryId);
    }


    @Override
    public List<CategoryResponse> getCategories(CustomUser customUser) {
        Member member = generateMember(customUser);
        return categoryRepository.findByMember(member)
            .stream()
            .map(CategoryResponse::from)
            .toList();
    }

    private Member generateMember(CustomUser customUser) {
        return memberRepository.findByEmail(customUser.getUsername())
            .orElseThrow(CMissingDataException::new);
    }

    private boolean validateUpdate(CategoryUpdateRequest request, CustomUser customUser) {
        Long userMemberId = generateMember(customUser).getId();
        return userMemberId.equals(request.getId());
    }

    private boolean validateDelete(CustomUser customUser, Long commentId) {
        long userMemberId = generateMember(customUser).getId();
        long categoryMemberId = categoryRepository.findById(commentId)
            .orElseThrow(CMissingDataException::new)
            .getId();
        return userMemberId == categoryMemberId;
    }
}
