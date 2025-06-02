package com.example.despesas_projeto.web;

import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.model.Transaction;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
public class ReceitaForm {
    @NotNull(message = "Data é obrigatória")
    private LocalDateTime data;

    @NotBlank(message = "Descrição é obrigatória")
    @Size(min = 3, max = 255, message = "Descrição deve ter entre 3 e 255 caracteres")
    private String descricao;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 10, fraction = 2, message = "Valor deve ter no máximo 10 dígitos inteiros e 2 casas decimais")
    private BigDecimal valor;

    @NotBlank(message = "Categoria é obrigatória")
    private String categoria;

    public Transaction toTransaction(String userId) {
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setTransactionDate(this.data);
        transaction.setDescription(this.descricao);
        transaction.setValue(this.valor);
        transaction.setType(TransactionType.INCOME);
        transaction.setCategory(this.categoria);
        transaction.setTags(new ArrayList<>());
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }
}