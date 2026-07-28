package com.rubencamero.finflow.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public sealed interface DomainEvent
        permits WalletCreated, WalletActivated, WalletFrozen, MoneyDeposited, MoneyWithdrawn {
    UUID eventId();
    LocalDateTime occurredOn();
}
