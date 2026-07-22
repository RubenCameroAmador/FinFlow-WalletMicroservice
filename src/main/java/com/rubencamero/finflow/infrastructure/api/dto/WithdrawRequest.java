package com.rubencamero.finflow.infrastructure.api.dto;

import java.math.BigDecimal;

public record WithdrawRequest(
        BigDecimal amount,
        String currency
) {
}
