package com.rubencamero.finflow.infrastructure.api.dto;

import java.math.BigDecimal;

public record DepositRequest(
        BigDecimal amount,
        String currency
) {
}
