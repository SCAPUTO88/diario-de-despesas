package com.example.despesas_projeto.controller;

import com.example.despesas_projeto.dto.TransactionRequestDTO;
import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.model.Transaction;
import com.example.despesas_projeto.service.TransactionService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    @Test
    void createTransaction_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        TransactionRequestDTO request = new TransactionRequestDTO(
                "user123",
                now,
                "Descrição",
                BigDecimal.TEN,
                TransactionType.INCOME,
                "Categoria",
                List.of("tag1", "tag2")
        );

        Transaction mockTransaction = Transaction.builder()
                .userId("user123")
                .transactionDate(now)
                .description("Descrição")
                .value(BigDecimal.TEN)
                .type(TransactionType.INCOME)
                .category("Categoria")
                .tags(List.of("tag1", "tag2"))
                .createdAt(now)
                .build();

        when(transactionService.createTransaction(any())).thenReturn(mockTransaction);

        // Act & Assert
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void getTransaction_ExistingTransaction_ShouldReturnOk() throws Exception {
        // Arrange
        String userId = "user123";
        LocalDateTime now = LocalDateTime.now();
        Transaction mockTransaction = Transaction.builder()
                .userId(userId)
                .transactionDate(now)
                .description("Descrição")
                .value(BigDecimal.TEN)
                .type(TransactionType.INCOME)  // Definindo o tipo
                .category("Categoria")
                .createdAt(now)
                .build();

        when(transactionService.findById(userId, now)).thenReturn(Optional.of(mockTransaction));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{userId}/{transactionDate}", userId, now)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void getUserTransactionsSummary_ShouldReturnOk() throws Exception {
        // Arrange
        String userId = "user123";
        List<Transaction> mockTransactions = new ArrayList<>();
        Transaction income = Transaction.builder()
                .userId(userId)
                .transactionDate(LocalDateTime.now())
                .description("Receita")
                .value(BigDecimal.TEN)
                .type(TransactionType.INCOME)
                .category("Categoria")
                .build();
        mockTransactions.add(income);

        when(transactionService.findAllByUserId(userId)).thenReturn(mockTransactions);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{userId}/summary", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReceitas").value(10))
                .andExpect(jsonPath("$.totalDespesas").value(0))
                .andExpect(jsonPath("$.saldo").value(10));
    }

}