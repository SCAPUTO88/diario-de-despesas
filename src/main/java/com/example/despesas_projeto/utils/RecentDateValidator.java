package com.example.despesas_projeto.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class RecentDateValidator implements ConstraintValidator<RecentDate, LocalDateTime> {
    private int yearsBack;

    @Override
    public void initialize(RecentDate constraintAnnotation) {
        this.yearsBack = constraintAnnotation.yearsBack();
    }

    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // @NotNull já trata isso
        }
        LocalDateTime minDate = LocalDateTime.now().minusYears(yearsBack);
        return !value.isBefore(minDate);
    }
}
