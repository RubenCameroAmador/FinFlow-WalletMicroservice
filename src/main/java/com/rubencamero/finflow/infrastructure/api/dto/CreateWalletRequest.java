package com.rubencamero.finflow.infrastructure.api.dto;

import java.math.BigDecimal;

public record CreateWalletRequest(
        String ownerId,
        BigDecimal amount,
        String currency
) {}
