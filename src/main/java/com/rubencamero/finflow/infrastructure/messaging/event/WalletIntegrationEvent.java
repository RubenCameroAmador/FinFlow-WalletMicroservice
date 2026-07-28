package com.rubencamero.finflow.infrastructure.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

public interface WalletIntegrationEvent {
    WalletEventType eventType();
    UUID eventId();
    LocalDateTime occurredOn();
    UUID walletId();
}
