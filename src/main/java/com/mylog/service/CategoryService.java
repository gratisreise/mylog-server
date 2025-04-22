package com.mylog.service;

import com.mylog.dto.category.CategoryCreateRequest;
import com.mylog.dto.category.CategoryResponse;
import com.mylog.dto.category.CategoryUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Category;
import com.mylog.entity.Member;
import com.mylog.exception.CReachedLimitException;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    private final int limit = 20;

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

    };

    //카테고리 삭제
    @Transactional
    public void deleteCategory(CustomUser customUser, Long commentId){

    };


    private Member generateMember(CustomUser customUser) {
        return memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
    }

}
