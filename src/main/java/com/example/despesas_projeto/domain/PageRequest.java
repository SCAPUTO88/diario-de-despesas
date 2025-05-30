package com.example.despesas_projeto.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageRequest {
    private int page;
    private int size;
    private String sortBy;
    private String sortDirection;

    public static PageRequest of(int page, int size) {
        return PageRequest.builder()
            .page(page)
            .size(size)
            .build();
    }

    public static PageRequest of(int page, int size, String sortBy, String sortDirection) {
        return PageRequest.builder()
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();
    }
}
