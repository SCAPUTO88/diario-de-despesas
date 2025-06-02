package com.example.despesas_projeto.controller;

import com.example.despesas_projeto.model.Transaction;
import com.example.despesas_projeto.service.SecurityService;
import com.example.despesas_projeto.service.TransactionService;
import com.example.despesas_projeto.web.ReceitaFiltro;
import com.example.despesas_projeto.web.ReceitaForm;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/receitas")
public class ReceitaController {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private SecurityService securityService;

    @GetMapping("/nova")
    public String novaReceita(Model model) {
        model.addAttribute("receita", new ReceitaForm());
        return "receitas/form";
    }

    @GetMapping
    public String listarReceitas(@ModelAttribute ReceitaFiltro filtro, Model model) {
        // Adiciona o filtro ao modelo
        model.addAttribute("filtro", filtro);

        // TODO: Buscar dados do backend usando os filtros
        List<Transaction> transactions = new ArrayList<>();
        model.addAttribute("receitas", transactions);

        // Informações de paginação
        model.addAttribute("paginaAtual", filtro.getPagina());
        model.addAttribute("tamanhoPagina", filtro.getTamanho());
        model.addAttribute("totalPaginas", 5);

        // Adiciona os parâmetros de ordenação
        model.addAttribute("ordenacaoAtual", filtro.getOrdenacao());
        model.addAttribute("direcaoAtual", filtro.getDirecao());

        return "receitas/lista";
    }

    @ModelAttribute("categorias")
    public List<String> categorias() {
        return Arrays.asList(
                "SALARIO",
                "INVESTIMENTOS",
                "FREELANCER",
                "VENDAS",
                "ALUGUEL",
                "OUTROS"
        );
    }

    @PostMapping("/salvar")
    public String salvarReceita(@Valid @ModelAttribute("receita") ReceitaForm form,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "receitas/form";
        }

        try {
            String userId = securityService.getCurrentUserId();
            log.info("Salvando receita para usuário: {}", userId);

            Transaction transaction = form.toTransaction(userId);
            Transaction saved = transactionService.createTransaction(transaction);
            log.info("Receita salva com sucesso: {}", saved.getTransactionDate());

            redirectAttributes.addFlashAttribute("mensagem", "Receita salva com sucesso!");
            return "redirect:/receitas";
        } catch (Exception e) {
            log.error("Erro ao salvar receita: {}", e.getMessage(), e);
            result.reject("error.global", "Erro ao salvar receita: " + e.getMessage());
            return "receitas/form";
        }

    }
}