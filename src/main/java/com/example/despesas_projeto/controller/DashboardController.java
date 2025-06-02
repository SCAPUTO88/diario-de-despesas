package com.example.despesas_projeto.controller;

import com.example.despesas_projeto.domain.Page;
import com.example.despesas_projeto.domain.PageRequest;
import com.example.despesas_projeto.domain.TransactionFilter;
import com.example.despesas_projeto.model.Transaction;
import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.service.SecurityService;
import com.example.despesas_projeto.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class DashboardController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SecurityService securityService;

    private void logTransactionData(List<Transaction> transactions) {
        log.debug("Número de transações encontradas: {}", transactions.size());
        transactions.forEach(t ->
                log.debug("Transação: userId={}, data={}, tipo={}, valor={}, categoria={}",
                        t.getUserId(),
                        t.getTransactionDate(),
                        t.getType(),
                        t.getValue(),
                        t.getCategory())
        );
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        try {
            String userId = securityService.getCurrentUserId();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                    .withHour(23).withMinute(59).withSecond(59);

            // Valores default para o caso de não haver dados
            BigDecimal totalDespesas = BigDecimal.ZERO;
            BigDecimal totalReceitas = BigDecimal.ZERO;
            BigDecimal saldo = BigDecimal.ZERO;
            BigDecimal mediaDespesas = BigDecimal.ZERO;

            // Buscar transações do mês atual
            List<Transaction> transactions = transactionService.findByDateRange(
                    userId, startOfMonth, endOfMonth);

            try {
                // Calcular totais do mês
                totalDespesas = sumTransactionsByType(transactions, TransactionType.EXPENSE);
                totalReceitas = sumTransactionsByType(transactions, TransactionType.INCOME);
                saldo = totalReceitas.subtract(totalDespesas);

                // Calcular média de despesas dos últimos 6 meses
                LocalDateTime sixMonthsAgo = now.minus(6, ChronoUnit.MONTHS);
                List<Transaction> lastSixMonthsTransactions = transactionService.findByDateRange(
                        userId, sixMonthsAgo, now);

                mediaDespesas = calculateAverageExpenses(lastSixMonthsTransactions);

                Map<String, Object> dadosEvolucaoMensal = getMonthlyEvolutionData(userId, sixMonthsAgo, now);
                Map<String, BigDecimal> dadosCategorias = getCategoryData(transactions);

                log.debug("Dados evolução mensal: {}", dadosEvolucaoMensal);
                log.debug("Dados categorias: {}", dadosCategorias);

                model.addAttribute("dadosEvolucaoMensal", dadosEvolucaoMensal);
                model.addAttribute("dadosCategorias", dadosCategorias);


                // Últimas transações
                TransactionFilter filter = TransactionFilter.builder()
                        .userId(userId)
                        .build();

                Page<Transaction> ultimasTransacoes = transactionService.findAllWithFilters(
                        filter,
                        PageRequest.builder()
                                .page(0)
                                .size(5)
                                .sortBy("transactionDate")
                                .sortDirection("desc")
                                .build()
                );
                model.addAttribute("ultimasTransacoes", ultimasTransacoes.getContent());
                logTransactionData(transactions);

            } catch (Exception e) {
                log.error("Erro ao buscar dados: {}", e.getMessage());
                // Em caso de erro, usamos os valores default inicializados acima
            }

            // Adiciona os valores ao modelo (mesmo em caso de erro)
            model.addAttribute("totalDespesas", totalDespesas);
            model.addAttribute("totalReceitas", totalReceitas);
            model.addAttribute("saldo", saldo);
            model.addAttribute("mediaDespesas", mediaDespesas);

            // Garante que há dados vazios para os gráficos em caso de erro
            if (!model.containsAttribute("dadosEvolucaoMensal")) {
                model.addAttribute("dadosEvolucaoMensal", new HashMap<String, List<BigDecimal>>());
            }
            if (!model.containsAttribute("dadosCategorias")) {
                model.addAttribute("dadosCategorias", new HashMap<String, BigDecimal>());
            }
            if (!model.containsAttribute("ultimasTransacoes")) {
                model.addAttribute("ultimasTransacoes", new ArrayList<>());
            }

            // Dados para o gráfico de evolução mensal (últimos 6 meses)
            LocalDateTime sixMonthsAgo = now.minus(6, ChronoUnit.MONTHS);
            Map<String, Object> dadosEvolucaoMensal = getMonthlyEvolutionData(userId, sixMonthsAgo, now);
            model.addAttribute("dadosEvolucaoMensal", dadosEvolucaoMensal);


            // Dados para o gráfico de categorias (usando as transações do mês atual)
            Map<String, BigDecimal> dadosCategorias = getCategoryData(transactions);
            model.addAttribute("dadosCategorias", dadosCategorias);

            // Últimas transações
            TransactionFilter filter = TransactionFilter.builder()
                    .userId(userId)
                    .build();

            Page<Transaction> ultimasTransacoes = transactionService.findAllWithFilters(
                    filter,
                    PageRequest.builder()
                            .page(0)
                            .size(5)
                            .sortBy("transactionDate")
                            .sortDirection("desc")
                            .build()
            );
            model.addAttribute("ultimasTransacoes", ultimasTransacoes.getContent());

            return "dashboard";

        } catch (Exception e) {
            log.error("Erro ao carregar dashboard", e);
            throw e;
        }
    }


    private BigDecimal sumTransactionsByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateAverageExpenses(List<Transaction> transactions) {
        BigDecimal totalDespesas = sumTransactionsByType(transactions, TransactionType.EXPENSE);
        return totalDespesas.divide(BigDecimal.valueOf(6), 2, BigDecimal.ROUND_HALF_UP);
    }

    private Map<String, Object> getMonthlyEvolutionData(String userId, LocalDateTime start, LocalDateTime end) {
        List<Transaction> transactions = transactionService.findByDateRange(userId, start, end);

        // Criar arrays para labels (meses) e dados
        List<String> labels = new ArrayList<>();
        List<BigDecimal> receitas = new ArrayList<>();
        List<BigDecimal> despesas = new ArrayList<>();

        // Formato para os meses
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM/yy", new Locale("pt", "BR"));

        // Para cada mês no período
        LocalDateTime current = start;
        while (!current.isAfter(end)) {
            final LocalDateTime monthStart = current.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            final LocalDateTime monthEnd = current.withDayOfMonth(current.toLocalDate().lengthOfMonth())
                    .withHour(23).withMinute(59).withSecond(59);

            // Filtrar transações do mês
            List<Transaction> monthTransactions = transactions.stream()
                    .filter(t -> !t.getTransactionDate().isBefore(monthStart) && !t.getTransactionDate().isAfter(monthEnd))
                    .collect(Collectors.toList());

            // Calcular totais
            BigDecimal monthlyIncome = monthTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.INCOME)
                    .map(Transaction::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal monthlyExpense = monthTransactions.stream()
                    .filter(t -> t.getType() == TransactionType.EXPENSE)
                    .map(Transaction::getValue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Adicionar aos arrays
            labels.add(current.format(monthFormatter));
            receitas.add(monthlyIncome);
            despesas.add(monthlyExpense);

            // Avançar para o próximo mês
            current = current.plusMonths(1);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("labels", labels);
        result.put("receitas", receitas);
        result.put("despesas", despesas);

        return result;
    }

    private Map<String, BigDecimal> getCategoryData(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.mapping(
                                Transaction::getValue,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));
    }
}