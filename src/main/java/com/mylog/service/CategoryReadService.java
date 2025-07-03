package com.mylog.service;

import com.mylog.model.dto.category.CategoryResponse;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Category;
import com.mylog.model.entity.Member;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.CategoryRepository;
import com.mylog.repository.MemberRepository;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryReadService {

    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getCategories(CustomUser customUser){
        Member member = generateMember(customUser);
        return categoryRepository.findByMember(member)
            .stream()
            .map(CategoryResponse::from)
            .toList();
    }

    private Member generateMember(CustomUser customUser) {
        return memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
    }

    public Category getByCategoryName(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName)
            .orElseThrow(CMissingDataException::new);
    }
}
