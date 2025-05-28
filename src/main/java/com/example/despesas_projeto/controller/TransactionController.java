package com.example.despesas_projeto.controller;


import com.example.despesas_projeto.dto.TransactionRequestDTO;
import com.example.despesas_projeto.dto.TransactionResponseDTO;
import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.model.Transaction;
import com.example.despesas_projeto.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponseDTO> createTransaction(@Valid @RequestBody TransactionRequestDTO request) {
        log.info("Criando nova transação para o usuário {}", request.userId());

        // Validações adicionais para garantir que os campos obrigatórios estejam presentes
        if (request.userId() == null || request.userId().trim().isEmpty()) {
            throw new IllegalArgumentException("userId é obrigatório");
        }

        if (request.transactionDate() == null) {
            throw new IllegalArgumentException("transactionDate é obrigatório");
        }

        Transaction transaction = toEntity(request);
        log.info("Transação convertida, pronta para salvar: userId={}, date={}, type={}, value={}",
                transaction.getUserId(),
                transaction.getTransactionDate(),
                transaction.getType(),
                transaction.getValue());


        // Validação adicional após a conversão
        if (transaction.getUserId() == null || transaction.getTransactionDate() == null) {
            throw new IllegalArgumentException("Falha ao mapear userId ou transactionDate");
        }

        Transaction createdTransaction = transactionService.createTransaction(transaction);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(TransactionResponseDTO.fromModel(createdTransaction));
    }


    @GetMapping("/{userId}/{transactionDate}")
    public ResponseEntity<TransactionResponseDTO> getTransction(
            @PathVariable String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime transactionDate) {

        log.debug(("Buscando transação para o usuário {} e data {}"), userId, transactionDate);

        return transactionService.findById(userId, transactionDate)
                .map(TransactionResponseDTO::fromModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<TransactionResponseDTO>> getAllTransactionsByUser(@PathVariable String userId) {
        log.info("Listando todas as transações para o usuário: {}", userId);

        List<TransactionResponseDTO> transactions = transactionService.findAllByUserId(userId).stream()
                .map(TransactionResponseDTO::fromModel)
                .toList();

        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{userId}/{transactionDate}")
    public ResponseEntity<TransactionResponseDTO> updateTransaction(
            @PathVariable String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime transactionDate,
            @Valid @RequestBody TransactionRequestDTO request) {

        log.debug("Atualizando transação para o usuário {} e data {}", userId, transactionDate);

        Transaction transaction = toEntity(request);
        transaction.setUserId(userId);
        transaction.setTransactionDate(transactionDate);

        Transaction updatedTransaction = transactionService.updateTransaction(transaction);
        return ResponseEntity.ok(TransactionResponseDTO.fromModel(updatedTransaction));
    }

    @DeleteMapping("/{userId}/{transactionDate}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime transactionDate) {

        log.info("Deletando transação para o usuário: {}, Data {}", userId, transactionDate);

        transactionService.delete(userId, transactionDate);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionByType(
            @PathVariable String userId,
            @PathVariable TransactionType type) {

        log.debug("Buscando transações do tipo {} para o usuário: {}", type, userId);

        List<TransactionResponseDTO> transactions = transactionService.findByType(userId, type).stream()
                .map(TransactionResponseDTO::fromModel)
                .toList();

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/category/{category}")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionByCategory(
            @PathVariable String userId,
            @PathVariable String category) {

        log.debug("Buscando transações da categoria '{}' para o usuário: {}", category, userId);

        List<TransactionResponseDTO> transactions = transactionService.findByCategory(userId, category).stream()
                .map(TransactionResponseDTO::fromModel)
                .toList();

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionsByDateRange(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.debug("Buscando transações no período de {} até {} para o usuário: {}", startDate, endDate, userId);

        List<TransactionResponseDTO> transactions = transactionService
                .findByDateRange(userId, startDate, endDate).stream()
                .map(TransactionResponseDTO::fromModel)
                .toList();

        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/summary/{year}/{month}")
    public ResponseEntity<Map<String, BigDecimal>> getMonthlyBalance(
            @PathVariable String userId,
            @PathVariable int year,
            @PathVariable int month) {

        log.info("Gerando resumo mensal para o usuário: {}, mês: {}/{}", userId, month, year);

        return ResponseEntity.ok(transactionService.getMonthlyBalance(userId, year, month));
    }

    @GetMapping("/user/{userId}/category-summary/{year}/{month}")
    public ResponseEntity<Map<String, BigDecimal>> getCategorySummary(
            @PathVariable String userId,
            @PathVariable int year,
            @PathVariable int month) {

        log.debug("Gerando resumo por categoria para o usuário: {}, mês: {}/{}", userId, month, year);

        return ResponseEntity.ok(transactionService.getCategorySummary(userId, year, month));
    }


//    private Transaction toEntity(TransactionRequestDTO  request) {
//        Transaction transaction = new Transaction();
//        transaction.setUserId(request.userId());
//        transaction.setTransactionDate(request.transactionDate());
//        transaction.setDescription(request.description());
//        transaction.setValue(request.value());
//        transaction.setCategory(request.category());
//        transaction.setType(TransactionType.valueOf(String.valueOf(request.type())));
//
//        if(request.tags() != null) {
//            for (String tag : request.tags()) {
//                transaction.addTag(tag);
//            }
//        }
//
//        return transaction;
//    }

    private Transaction toEntity(TransactionRequestDTO request) {
        log.debug("Iniciando conversão do DTO para Entity. DTO: {}", request);

        Transaction transaction = new Transaction();

        log.debug("Setando userId: {}", request.userId());
        transaction.setUserId(request.userId());

        log.debug("Setando transactionDate: {}", request.transactionDate());
        transaction.setTransactionDate(request.transactionDate());

        log.debug("Setando description: {}", request.description());
        transaction.setDescription(request.description());

        log.debug("Setando value: {}", request.value());
        transaction.setValue(request.value());

        log.debug("Setando category: {}", request.category());
        transaction.setCategory(request.category());

        log.debug("Setando type: {}", request.type());
        transaction.setType(TransactionType.valueOf(String.valueOf(request.type())));

        if(request.tags() != null) {
            log.debug("Setando tags: {}", request.tags());
            for (String tag : request.tags()) {
                transaction.addTag(tag);
            }
        }

        log.debug("Entity convertida: {}", transaction);
        return transaction;
    }

}
