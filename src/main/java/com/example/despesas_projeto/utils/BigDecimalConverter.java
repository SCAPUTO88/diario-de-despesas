package com.example.despesas_projeto.utils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import java.math.BigDecimal;

public class BigDecimalConverter implements DynamoDBTypeConverter<String, BigDecimal> {

    @Override
    public String convert(BigDecimal value) {
        return value != null ? value.toString() : null;
    }

    @Override
    public BigDecimal unconvert(String value) {
        return value != null ? new BigDecimal(value) : null;
    }
}