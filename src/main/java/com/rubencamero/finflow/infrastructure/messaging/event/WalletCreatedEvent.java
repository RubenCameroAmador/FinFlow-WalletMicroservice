package com.rubencamero.finflow.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WalletCreatedEvent(
        WalletEventType eventType,
        UUID eventId,
        LocalDateTime occurredOn,
        UUID walletId,
        UUID ownerId,
        BigDecimal initialBalanceAmount,
        String initialBalanceCurrency
) implements WalletIntegrationEvent {

    public WalletCreatedEvent(UUID eventId, LocalDateTime occurredOn, UUID walletId, UUID ownerId,
                              BigDecimal initialBalanceAmount, String initialBalanceCurrency) {
        this(WalletEventType.WALLET_CREATED, eventId, occurredOn, walletId, ownerId,
                initialBalanceAmount, initialBalanceCurrency);
    }
}