package com.example.despesas_projeto.domain;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Data
@Builder
public class Page<T> {
    private List<T> content;
    private Integer pageNumber;
    private Integer pageSize;
    private Long totalElements;
    private Integer totalPages;
    private Boolean last;
    private Boolean first;

    public static <T> Page<T> of(List<T> content, PageRequest pageRequest, long total) {
        int totalPages = (int) Math.ceil((double) total / pageRequest.getSize());

        return Page.<T>builder()
                .content(content)
                .pageNumber(pageRequest.getPage())
                .pageSize(pageRequest.getSize())
                .totalElements(total)
                .totalPages(totalPages)
                .first(pageRequest.getPage() == 0)
                .last(pageRequest.getPage() == totalPages - 1)
                .build();
    }

    // Método para compatibilidade com Spring Data
    public int getNumber() {
        return pageNumber != null ? pageNumber : 0;
    }

    // Método auxiliar para verificar se há próxima página
    public boolean hasNext() {
        return !last;
    }

    // Método auxiliar para verificar se há página anterior
    public boolean hasPrevious() {
        return !first;
    }

    // Método para obter o número da próxima página
    public int getNextPageNumber() {
        return hasNext() ? getNumber() + 1 : getNumber();
    }

    // Método para obter o número da página anterior
    public int getPreviousPageNumber() {
        return hasPrevious() ? getNumber() - 1 : 0;
    }
}