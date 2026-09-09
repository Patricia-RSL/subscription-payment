package com.patricia.subscriptionApp.enums;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum PlanType {

    BASICO(new BigDecimal("19.90")),
    PREMIUM(new BigDecimal("39.90")),
    FAMILIA(new BigDecimal("59.90"));

    private final BigDecimal valor;

    PlanType(BigDecimal valor) {
        this.valor = valor;
    }
}
