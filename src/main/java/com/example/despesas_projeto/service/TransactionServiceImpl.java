package com.example.despesas_projeto.service;

import com.example.despesas_projeto.domain.Page;
import com.example.despesas_projeto.domain.PageRequest;
import com.example.despesas_projeto.domain.TransactionFilter;
import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.exception.InvalidTransactionException;
import com.example.despesas_projeto.exception.TransactionNotFoundException;
import com.example.despesas_projeto.model.Transaction;
import com.example.despesas_projeto.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;

    @Override
    public Transaction createTransaction(Transaction transaction) {
        log.info("Criando nova transação para o usuário {}", transaction.getUserId());
        validateTransaction(transaction);

        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(LocalDateTime.now());
            log.debug("Definindo data de criação da transação {}", transaction.getCreatedAt());
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transação criada com sucesso. ID: {}, Data: {}", savedTransaction.getUserId(), savedTransaction.getTransactionDate());

        return savedTransaction;
    }

    @Override
    public Optional<Transaction> findById(String userId, LocalDateTime transactionDate) {
        log.debug("Buscando transação para o usuário {} e data {}", userId, transactionDate);
        return transactionRepository.findById(userId, transactionDate);
    }

    @Override
    public List<Transaction> findAllByUserId(String userId) {
        log.info("Listando todas as transações para o usuário: {}", userId);
        return transactionRepository.findByUserId(userId);
    }

    @Override
    public Transaction updateTransaction(Transaction transaction) {
        log.info("Atualizando transação para o usuário: {}, Data {}", transaction.getUserId(), transaction.getTransactionDate());
        validateTransaction(transaction);

        if(transactionRepository.existsById(transaction.getUserId(), transaction.getTransactionDate())) {
            log.error("Transação para o usuário {} e data {} não encontrada", transaction.getUserId(), transaction.getTransactionDate());
            throw new TransactionNotFoundException("Transação não encontrada");
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);
        log.info("Transação atualizada com sucesso. ID: {}, Data: {}", updatedTransaction.getUserId(), updatedTransaction.getTransactionDate());

        return updatedTransaction;
    }

    @Override
    public void delete(String userId, LocalDateTime transactionDate) {
        log.info("Deletando transação para o usuário: {}, Data {}", userId, transactionDate);

        Transaction transaction = transactionRepository.findById(userId, transactionDate)
                .orElseThrow(() -> {
                    log.error("Transação não encontrada para remoção. Usuário: {}, Data: {}", userId, transactionDate);
                        return new TransactionNotFoundException("Transação nao encontrada");
    });

        transactionRepository.delete(transaction);
        log.info("Transação deletada com sucesso. ID: {}, Data: {}", transaction.getUserId(), transaction.getTransactionDate());
    }

    @Override
    public List<Transaction> findByType(String userId, TransactionType type) {
        log.debug("Buscando transações do tipo {} para o usuário: {}", type, userId);
        return transactionRepository.findByUserIdAndType(userId, type);
    }

    @Override
    public List<Transaction> findByCategory(String userId, String category) {
        log.debug("Buscando transações da categoria '{}' para o usuário: {}", category, userId);
        return transactionRepository.findByUserIdAndCategory(userId, category);
    }

    @Override
    public List<Transaction> findByDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Buscando transações no período de {} até {} para o usuário: {}", startDate, endDate, userId);
        return transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Override
    public BigDecimal calculateTotalByType(String userId, TransactionType type, int year, int month) {
        log.debug("Calculando total de {} para o usuário: {}, mês: {}/{}", type, userId, month, year);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        return transactions.stream()
                .filter(t -> t.getType() == type) // Alterado aqui
                .map(Transaction::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }



    @Override
    public Map<String, BigDecimal> getCategorySummary(String userId, int year, int month) {
        log.debug("Gerando resumo por categoria para o usuário: {}, mês: {}/{}", userId, month, year);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Transaction> transactions = transactionRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        return transactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getValue,
                                BigDecimal::add
                        )
                ));
    }

    @Override
    public Map<String, BigDecimal> getMonthlyBalance(String userId, int year, int month) {
        log.info("Calculando saldo mensal para o usuário: {}, mês: {}/{}", userId, month, year);

        BigDecimal receitas = calculateTotalByType(userId, TransactionType.INCOME, year, month);
        BigDecimal despesas = calculateTotalByType(userId, TransactionType.EXPENSE, year, month);
        BigDecimal saldo = receitas.subtract(despesas);

        log.debug("Resumo mensal - Receitas: {}, Despesas: {}, Saldo: {}", receitas, despesas, saldo);

        return Map.of(
                "receitas", receitas,
                "despesas", despesas,
                "saldo", saldo
        );
    }

    private record ValidationRule(String fieldName, Object value, String errorMessage) {
        boolean isValid() {
         if(value == null) return false;
         if(value instanceof String) {
             return !((String) value).trim().isEmpty();
         }
         return true;
        }
    }

    @Override
    public void validateTransaction(Transaction transaction) {
        log.debug("Validando transação: {}", transaction);

        if(transaction == null) {
            throw new InvalidTransactionException("Transação inválida. Não pode ser nula");
        }

        List<ValidationRule> rules = List.of(
                new ValidationRule("ID do usuário", transaction.getUserId(), "ID do usuário é obrigatório"),
                new ValidationRule("Data da transação", transaction.getTransactionDate(), "Data da transação é obrigatória"),
                new ValidationRule("Descrição", transaction.getDescription(), "Descrição da transação é obrigatória"),
                new ValidationRule("Categoria", transaction.getCategory(), "Categoria da transação é obrigatória")
        );

        rules.stream()
                .filter(rule -> !rule.isValid())
                .findFirst()
                .ifPresent(rule -> {
                    throw new InvalidTransactionException(rule.errorMessage());
                });

        if(transaction.getValue() == null || transaction.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            String message = "Valor da transação deve ser maior que zero";
            log.error(message);
            throw new InvalidTransactionException(message);
        }

        if(transaction.getType() == null) {
            String message = "Tipo da transação é obrigatório";
            log.error(message);
            throw new InvalidTransactionException(message);
        }

        log.debug("Transação validada com sucesso");
    }

    @Override
    public Page<Transaction> findAllWithFilters(TransactionFilter filter, PageRequest pageRequest) {
        log.debug("Buscando transações com filtros: {}, paginação: {}", filter, pageRequest);

        // Implementar a lógica de filtragem e paginação
        List<Transaction> transactions = transactionRepository.findByFilters(filter);

        // Aplicar ordenação
        if (pageRequest.getSortBy() != null) {
            transactions = sortTransactions(transactions, pageRequest);
        }

        // Aplicar paginação
        int start = pageRequest.getPage() * pageRequest.getSize();
        int end = Math.min(start + pageRequest.getSize(), transactions.size());
        List<Transaction> pagedContent = transactions.subList(start, end);

        return Page.of(pagedContent, pageRequest, transactions.size());
    }

    @Override
    public List<String> findAllCategories(String userId) {
        log.debug("Buscando todas as categorias para o usuário: {}", userId);
        return transactionRepository.findByUserId(userId).stream()
                .map(Transaction::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, BigDecimal> getMonthlyTrendByCategory(String userId, String category, int year) {
        log.debug("Calculando tendência mensal para categoria {} do usuário {} no ano {}",
                category, userId, year);

        Map<String, BigDecimal> monthlyTrend = new LinkedHashMap<>();

        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            List<Transaction> transactions = transactionRepository.findByUserIdAndDateRange(
                    userId, startDate, endDate);

            BigDecimal total = transactions.stream()
                    .filter(t -> category.equals(t.getCategory()))
                    .map(Transaction::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            monthlyTrend.put(yearMonth.toString(), total);
        }

        return monthlyTrend;
    }

    private List<Transaction> sortTransactions(List<Transaction> transactions, PageRequest pageRequest) {
        Comparator<Transaction> comparator = switch (pageRequest.getSortBy()) {
            case "value" -> Comparator.comparing(Transaction::getValue);
            case "date" -> Comparator.comparing(Transaction::getTransactionDate);
            case "category" -> Comparator.comparing(Transaction::getCategory);
            case "type" -> Comparator.comparing(t -> t.getType().name());
            default -> Comparator.comparing(Transaction::getTransactionDate);
        };

        if ("desc".equalsIgnoreCase(pageRequest.getSortDirection())) {
            comparator = comparator.reversed();
        }

        return transactions.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

}
