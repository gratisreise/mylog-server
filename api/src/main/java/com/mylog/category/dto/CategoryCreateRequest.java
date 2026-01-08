package com.mylog.category.dto;

import com.mylog.category.entity.Category;
import com.mylog.member.entity.Member;
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
