package com.mylog.service;

import com.mylog.dto.category.CategoryCreateRequest;
import com.mylog.dto.category.CategoryDeleteRequest;
import com.mylog.dto.category.CategoryResponse;
import com.mylog.dto.category.CategoryUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.exception.CInvalidDataException;
import com.mylog.exception.CMissingDataException;
import com.mylog.exception.CReachedLimitException;
import com.mylog.exception.CUnAuthorizedException;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    private final int limit = 20;
    public static String originCategory = "없음";
    //카테고리 생성
    @Transactional
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
    };

    @Transactional
    public void createCategory(String email){
        Member member = memberRepository.findByEmail(email).orElseThrow(CMissingDataException::new);

        Category category = Category.builder()
            .member(member)
            .categoryName(originCategory)
            .build();
        categoryRepository.save(category);
    }


    //카테고리 목록 조회
    public List<CategoryResponse> getCategories(CustomUser customUser){
        Member member = generateMember(customUser);
        return categoryRepository.findByMember(member)
            .stream()
            .map(CategoryResponse::from)
            .toList();
    };


    //카테고리 수정
    @Transactional
    public void updateCategory(CategoryUpdateRequest request, CustomUser customUser){
        if(!validateUpdate(request, customUser)){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        };

        categoryRepository.findById(request.getCategoryId())
            .orElseThrow(CMissingDataException::new)
            .update(request);
    };

    //카테고리 삭제
    @Transactional
    public void deleteCategory(CategoryDeleteRequest request, CustomUser customUser){
        if(!validateDelete(request, customUser)){
            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
        };
        if(!categoryRepository.existsById(request.getCategoryId())){
            throw new CInvalidDataException("존재하지 않는 카테고리입니다.");
        }

        categoryRepository.deleteById(request.getCategoryId());
    };


    private Member generateMember(CustomUser customUser) {
        return memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
    }

    private boolean validateUpdate(CategoryUpdateRequest request, CustomUser customUser) {
        return request.getMemberId().equals(customUser.getMemberId());
    }

    private boolean validateDelete(CategoryDeleteRequest request, CustomUser customUser) {
        return request.getMemberId().equals(customUser.getMemberId());
    }

}
