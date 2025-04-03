package com.mylog.service.category;


import com.mylog.dto.category.CategoryCreateRequest;
import com.mylog.dto.category.CategoryResponse;
import com.mylog.dto.category.CategoryUpdateRequest;
import com.mylog.dto.classes.CustomUser;
import java.util.List;

public interface CategoryService {
    void createCategory(CategoryCreateRequest request, CustomUser customUser);
    void updateCategory(CategoryUpdateRequest request, CustomUser customUser);
    void deleteCategory(CustomUser customUser);
    List<CategoryResponse> getCategories(CustomUser customUser);
}
