package com.rubencamero.finflow.infrastructure.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record WalletActivatedEvent(
        WalletEventType eventType,
        UUID eventId,
        LocalDateTime occurredOn,
        UUID walletId,
        UUID ownerId
) implements WalletIntegrationEvent {

    public WalletActivatedEvent(UUID eventId, LocalDateTime occurredOn, UUID walletId, UUID ownerId) {
        this(WalletEventType.WALLET_ACTIVATED, eventId, occurredOn, walletId, ownerId);
    }
}