package com.rubencamero.finflow.infrastructure.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record WalletFrozenEvent(
        WalletEventType eventType,
        UUID eventId,
        LocalDateTime occurredOn,
        UUID walletId,
        UUID ownerId
) implements WalletIntegrationEvent {

    public WalletFrozenEvent(UUID eventId, LocalDateTime occurredOn, UUID walletId, UUID ownerId) {
        this(WalletEventType.WALLET_FROZEN, eventId, occurredOn, walletId, ownerId);
    }
}