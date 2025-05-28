package com.example.despesas_projeto.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = TransactionTypeValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidTransactionType {
    String message() default "Tipo de transação inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
