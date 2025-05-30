package com.example.despesas_projeto.controller;


import com.example.despesas_projeto.domain.Page;
import com.example.despesas_projeto.domain.PageRequest;
import com.example.despesas_projeto.domain.TransactionFilter;
import com.example.despesas_projeto.dto.TransactionRequestDTO;
import com.example.despesas_projeto.dto.TransactionResponseDTO;
import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.model.Transaction;
import com.example.despesas_projeto.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transações", description = "Endpoints para gerenciamento de transações financeirasz")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(
            summary = "Criação de transações",
            description = "Endpoint para criar transações financeiras"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Transação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })


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

    @Operation(
            summary = "Buscar transação específica",
            description = "Recupera uma transação específica com base no ID do usuário e data da transação"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transação encontrada"),
            @ApiResponse(responseCode = "404", description = "Transação não encontrada")
    })

    @GetMapping("/{userId}/{transactionDate}")
    public ResponseEntity<TransactionResponseDTO> getTransaction(
            @PathVariable String userId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime transactionDate) {

        log.debug(("Buscando transação para o usuário {} e data {}"), userId, transactionDate);

        return transactionService.findById(userId, transactionDate)
                .map(TransactionResponseDTO::fromModel)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/{userId}/summary")
    public ResponseEntity<Map<String, BigDecimal>> getUserTransactionsSummary(@PathVariable String userId) {
        log.info("Gerando resumo de transações para o usuário: {}", userId);

        List<Transaction> transactions = transactionService.findAllByUserId(userId);

        BigDecimal totalReceitas = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getValue)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        BigDecimal totalDespesas = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getValue)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        BigDecimal saldo = totalReceitas.subtract(totalDespesas);

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalReceitas", totalReceitas);
        summary.put("totalDespesas", totalDespesas);
        summary.put("saldo", saldo);

        return ResponseEntity.ok(summary);

    }

    @GetMapping("/{userId}/receitas/total")
    public ResponseEntity<BigDecimal> getTotalReceitas(@PathVariable String userId) {
        log.info("Calculando total de receitas para o usuário: {}", userId);

        BigDecimal total = transactionService.findAllByUserId(userId).stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getValue)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return ResponseEntity.ok(total);
    }

    @GetMapping("/{userId}/despesas/total")
    public ResponseEntity<BigDecimal> getTotalDespesas(@PathVariable String userId) {
        log.info("Calculando total de despesas para o usuário: {}", userId);

        BigDecimal total = transactionService.findAllByUserId(userId).stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getValue)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return ResponseEntity.ok(total);
    }

    @GetMapping("/{userId}/saldo")
    public ResponseEntity<BigDecimal> getSaldo(@PathVariable String userId) {
        log.info("Calculando saldo para o usuário: {}", userId);

        List<Transaction> transactions = transactionService.findAllByUserId(userId);

        BigDecimal receitas = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getValue)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        BigDecimal despesas = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getValue)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return ResponseEntity.ok(receitas.subtract(despesas));
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

    @GetMapping("/{userId}/transactions")
    public ResponseEntity<Page<Transaction>> getTransactions(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) BigDecimal minValue,
            @RequestParam(required = false) BigDecimal maxValue,
            @RequestParam(required = false) String description) {

        TransactionFilter filter = TransactionFilter.builder()
                .userId(userId)
                .type(type)
                .category(category)
                .startDate(startDate)
                .endDate(endDate)
                .minValue(minValue)
                .maxValue(maxValue)
                .description(description)
                .build();

        PageRequest pageRequest = PageRequest.of(page, size, sortBy, sortDirection);

        return ResponseEntity.ok(transactionService.findAllWithFilters(filter, pageRequest));
    }


}
