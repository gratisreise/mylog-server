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
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@ServiceType(OauthProvider.SOCIAL)
@Transactional(readOnly = true)
public class SocialCategoryService implements CategoryService {
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void createCategory(CategoryCreateRequest request, CustomUser customUser) {
        //사용자 id 가져오기
        Member member = generateMember(customUser);

        //카테고리 객체 생성
        Category category = Category.builder()
            .categoryName(request.getCategoryName())
            .member(member)
            .build();

        categoryRepository.save(category);
    }


    @Override
    @Transactional
    public void updateCategory(CategoryUpdateRequest request, CustomUser customUser) {

    }

    @Override
    @Transactional
    public void deleteCategory(CustomUser customUser) {

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
        return memberRepository.findById(Long.valueOf(customUser.getUsername()))
            .orElseThrow(CMissingDataException::new);
    }

}
