package com.example.despesas_projeto.utils;

import com.example.despesas_projeto.enums.TransactionType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TransactionTypeValidator implements ConstraintValidator<ValidTransactionType, TransactionType> {
    @Override
    public boolean isValid(TransactionType value, ConstraintValidatorContext context) {
        return (value == TransactionType.INCOME || value == TransactionType.EXPENSE);
    }
}