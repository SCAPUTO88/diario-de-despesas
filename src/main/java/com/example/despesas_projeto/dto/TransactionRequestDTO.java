package com.example.despesas_projeto.dto;

import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.utils.RecentDate;
import com.example.despesas_projeto.utils.ValidTransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TransactionRequestDTO (
        @NotBlank(message = "ID do usuário é obrigatório")
        @Size(min = 1, max = 100, message = "ID do usuário deve ter entre 1 e 100 caracteres")
        String userId,

        @NotNull(message = "Data da transação é obrigatória")
        @PastOrPresent(message = "Data da transação não pode ser futura")
        @RecentDate(yearsBack = 1, message = "Data não pode ser anterior a 1 ano atrás")
        LocalDateTime transactionDate,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(min = 3, max = 255, message = "Descrição deve ter entre 3 e 255 caracteres")
        String description,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        @Digits(integer = 10, fraction = 2, message = "Valor deve ter no máximo 10 dígitos inteiros e 2 casas decimais")
        BigDecimal value,

        @NotNull(message = "Tipo da transação é obrigatório")
        @ValidTransactionType
        TransactionType type,

        @NotBlank(message = "Categoria é obrigatória")
        @Size(min = 2, max = 50, message = "Categoria deve ter entre 2 e 50 caracteres")
        String category,

        @Size(max = 10, message = "Máximo de 10 tags permitidas")
        List<@Size(min = 1, max = 20, message = "Cada tag deve ter entre 1 e 20 caracteres") String> tags
) {}



