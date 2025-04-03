package com.mylog.service.category;

import com.mylog.dto.category.CategoryCreateRequest;
import com.mylog.dto.category.CategoryResponse;
import com.mylog.dto.category.CategoryUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LocalCategoryService implements CategoryService {


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
