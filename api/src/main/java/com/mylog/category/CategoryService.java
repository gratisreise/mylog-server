package com.mylog.category;

import com.mylog.auth.classes.CustomUser;
import com.mylog.category.dto.CategoryCreateRequest;
import com.mylog.category.dto.CategoryResponse;
import com.mylog.category.entity.Category;
import com.mylog.category.service.CategoryReader;
import com.mylog.category.service.CategoryWriter;
import com.mylog.enums.ErrorMessage;
import com.mylog.exception.ReachedLimitException;
import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
import com.mylog.response.CommonValue;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryReader categoryReader;
    private final CategoryWriter categoryWriter;
    private final MemberReader memberReader;

    @Transactional
    public void createCategory(CategoryCreateRequest request, CustomUser customUser) {
        Member member =  memberReader.getById(customUser.getMemberId());

        //갯수체크
        if(categoryReader.categorySize(member) == CommonValue.CATEGORY_LIMIT){
            throw new ReachedLimitException(ErrorMessage.CATEGORY_REACHED_LIMIT);
        }

        Category category  = request.toEntity(member);

        categoryWriter.createCategory(category);
    }

    public List<CategoryResponse> getCategories(CustomUser customUser) {
        return categoryReader.getCategories(customUser.getMemberId())
            .stream()
            .map(CategoryResponse::from)
            .toList();
    }
}
