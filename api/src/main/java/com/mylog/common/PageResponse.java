package com.mylog.common;

import java.util.List;
import lombok.Builder;
import org.springframework.data.domain.Page;


public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious
) {
    @Builder
    public PageResponse { /* 자동생성 생성자로 인한 컴파일 에러를 피하기 위해 생성 */ }
    public static <T> PageResponse<T> from(Page<T> page) {
        return PageResponse.<T>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }
}
