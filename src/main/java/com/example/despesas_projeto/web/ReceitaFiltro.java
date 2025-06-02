package com.example.despesas_projeto.web;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ReceitaFiltro {
    private String descricao;
    private String categoria;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataInicial;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dataFinal;

    private String ordenacao = "data";
    private String direcao = "desc";
    private int pagina = 0;
    private int tamanho = 10;
}