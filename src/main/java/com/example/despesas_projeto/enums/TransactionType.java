package com.example.despesas_projeto.enums;

public enum TransactionType {
    INCOME("Receita"),
    EXPENSE("Despesa");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
