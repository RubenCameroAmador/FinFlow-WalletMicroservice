package com.rubencamero.finflow.infrastructure.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponse(
        String walletId,
        String ownerId,
        BigDecimal balance,
        String currency,
        String status,
        LocalDateTime createdAt
) {}
