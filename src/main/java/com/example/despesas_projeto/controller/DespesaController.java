package com.example.despesas_projeto.controller;

import com.example.despesas_projeto.domain.Page;
import com.example.despesas_projeto.domain.PageRequest;
import com.example.despesas_projeto.domain.TransactionFilter;
import com.example.despesas_projeto.enums.TransactionType;
import com.example.despesas_projeto.model.Transaction;
import com.example.despesas_projeto.service.SecurityService;
import com.example.despesas_projeto.service.TransactionService;
import com.example.despesas_projeto.web.DespesaFiltro;
import com.example.despesas_projeto.web.DespesaForm;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/despesas")
public class DespesaController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SecurityService securityService;


    @GetMapping("/nova")
    public String novaDespesa(Model model) {
        model.addAttribute("despesa", new DespesaForm());
        return "despesas/form";
    }

    @GetMapping
    public String listarDespesas(@ModelAttribute DespesaFiltro filtro, Model model) {
        try {
            String userId = securityService.getCurrentUserId();

            TransactionFilter transactionFilter = TransactionFilter.builder()
                    .userId(userId)
                    .description(filtro.getDescricao())
                    .category(filtro.getCategoria())
                    .startDate(filtro.getDataInicial() != null ?
                            filtro.getDataInicial().atStartOfDay() : null)
                    .endDate(filtro.getDataFinal() != null ?
                            filtro.getDataFinal().atTime(23, 59, 59) : null)
                    .type(TransactionType.EXPENSE)
                    .build();

            PageRequest pageRequest = PageRequest.builder()
                    .page(filtro.getPagina())
                    .size(filtro.getTamanho())
                    .sortBy(mapSortField(filtro.getOrdenacao()))
                    .sortDirection(filtro.getDirecao())
                    .build();

            Page<Transaction> resultado = transactionService.findAllWithFilters(
                    transactionFilter,
                    pageRequest
            );

            model.addAttribute("despesas", resultado.getContent());
            model.addAttribute("filtro", filtro);
            model.addAttribute("paginaAtual", resultado.getNumber());
            model.addAttribute("totalPaginas", resultado.getTotalPages());

            return "despesas/lista";
        } catch (Exception e) {
            log.error("Erro ao listar despesas", e);
            throw e;
        }
    }


    @ModelAttribute("categorias")
    public List<String> categorias() {
        return Arrays.asList(
                "MORADIA",
                "ALIMENTACAO",
                "TRANSPORTE",
                "SAUDE",
                "EDUCACAO",
                "LAZER",
                "OUTROS"
        );
    }

    @PostMapping("/salvar")
    public String salvarDespesa(@Valid @ModelAttribute("despesa") DespesaForm form,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "despesas/form";
        }

        try {
            String userId = securityService.getCurrentUserId();
            log.info("Salvando despesa para usuário: {}", userId);


            Transaction transaction = form.toTransaction(userId);

            Transaction saved = transactionService.createTransaction(transaction);

            log.info("Despesa salva com sucesso: {}", saved.getTransactionDate());

            redirectAttributes.addFlashAttribute("mensagem", "Despesa salva com sucesso!");
            return "redirect:/despesas";
        } catch (Exception e) {
            log.error("Erro ao salvar despesa: {}", e.getMessage(), e);

            result.reject("error.global", "Erro ao salvar despesa: " + e.getMessage());
            return "despesas/form";
        }
    }

    private String mapSortField(String campo) {
        return switch (campo) {
            case "data" -> "transactionDate";
            case "descricao" -> "description";
            case "valor" -> "value";
            default -> "transactionDate";
        };
    }

}