package com.mylog.category.service;


import com.mylog.category.entity.Category;
import com.mylog.category.repository.CategoryRepository;
import com.mylog.member.service.MemberReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryWriter {

    private final CategoryRepository categoryRepository;

    public void createCategory(Category category) {
        categoryRepository.save(category);
    }

//    public void createCategory(CategoryCreateRequest request, CustomUser customUser){
//        Member member = memberReader.getById(customUser.getMemberId());
//        int categorySize = categoryRepository.countByMember(member);
//
//        if(categorySize == CommonValue.CATEGORY_LIMIT){
//            throw new CReachedLimitException("카테고리 갯수가 한도에 도달했습니다.");
//        }
//
//        Category category = request.toEntity(member);
//
//        categoryRepository.save(category);
//    }
//
//    @Async
//    public void createCategory(Member member){
//        Category category = Category.builder()
//            .member(member)
//            .categoryName(CommonValue.ORIGIN_CATEGORY)
//            .build();
//        categoryRepository.save(category);
//    }
//
//    public void updateCategory(CategoryUpdateRequest request,Long categoryId, CustomUser customUser){
//        Category category = categoryReader.getById(categoryId);
//
//        if(!category.isOwnedBy(customUser.getMemberId())){
//            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
//        }
//
//        category.update(request);
//    }
//
//    public void deleteCategory(Long categoryId, CustomUser customUser){
//        Category category = categoryReader.getById(categoryId);
//
//        if(!category.isOwnedBy(customUser.getMemberId())){
//            throw new CUnAuthorizedException("허용되지 않는 유저입니다.");
//        }
//
//        categoryRepository.deleteById(categoryId);
//    }
}
