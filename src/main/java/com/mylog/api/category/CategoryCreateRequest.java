package com.mylog.api.category;

import com.mylog.domain.entity.Category;
import com.mylog.domain.entity.Member;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;


public record CategoryCreateRequest (
    @Length(min = 1, max = 12)
    @NotBlank
    String categoryName
){

    public Category toEntity(Member member) {
        return Category.builder()
            .member(member)
            .categoryName(categoryName)
            .build();
    }
}
