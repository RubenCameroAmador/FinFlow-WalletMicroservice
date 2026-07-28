package com.rubencamero.finflow.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record MoneyDepositedEvent(
        WalletEventType eventType,
        UUID eventId,
        LocalDateTime occurredOn,
        UUID walletId,
        UUID ownerId,
        BigDecimal previousAmount,
        BigDecimal depositedAmount,
        String currency
) implements WalletIntegrationEvent {

    public MoneyDepositedEvent(UUID eventId, LocalDateTime occurredOn, UUID walletId, UUID ownerId,
                               BigDecimal previousAmount, BigDecimal depositedAmount, String currency) {
        this(WalletEventType.MONEY_DEPOSITED, eventId, occurredOn, walletId, ownerId,
                previousAmount, depositedAmount, currency);
    }
}