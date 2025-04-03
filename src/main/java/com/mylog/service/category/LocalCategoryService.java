package com.mylog.service.category;

import com.mylog.annotations.ServiceType;
import com.mylog.dto.category.CategoryCreateRequest;
import com.mylog.dto.category.CategoryResponse;
import com.mylog.dto.category.CategoryUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import com.mylog.enums.OauthProvider;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@ServiceType(OauthProvider.LOCAL)
public class LocalCategoryService implements CategoryService {
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void createCategory(CategoryCreateRequest request, CustomUser customUser) {

    }

    @Override
    public void updateCategory(CategoryUpdateRequest request, CustomUser customUser) {

    }

    @Override
    public void deleteCategory(CustomUser customUser) {

    }

    @Override
    public List<CategoryResponse> getCategories(CustomUser customUser) {
        return List.of();
    }
}
