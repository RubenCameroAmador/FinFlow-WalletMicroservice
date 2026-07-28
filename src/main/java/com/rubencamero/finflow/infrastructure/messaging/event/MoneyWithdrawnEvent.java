package com.rubencamero.finflow.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MoneyWithdrawnEvent(
        WalletEventType eventType,
        UUID eventId,
        LocalDateTime occurredOn,
        UUID walletId,
        UUID ownerId,
        BigDecimal previousAmount,
        BigDecimal withdrawnAmount,
        String currency
) implements WalletIntegrationEvent {

    public MoneyWithdrawnEvent(UUID eventId, LocalDateTime occurredOn, UUID walletId, UUID ownerId,
                               BigDecimal previousAmount, BigDecimal withdrawnAmount, String currency) {
        this(WalletEventType.MONEY_WITHDRAWN, eventId, occurredOn, walletId, ownerId,
                previousAmount, withdrawnAmount, currency);
    }
}