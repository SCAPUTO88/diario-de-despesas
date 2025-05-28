package com.example.despesas_projeto.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RecentDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RecentDate {
    String message() default "Data muito antiga";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int yearsBack() default 10;
}
