package com.rubencamero.finflow.domain.event;

import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletId;

import java.time.LocalDateTime;
import java.util.UUID;

public record WalletCreated(
        UUID eventId,
        LocalDateTime occurredOn,
        WalletId walletId,
        OwnerId ownerId,
        Money initialBalance
) implements DomainEvent {

    public WalletCreated(WalletId walletId, OwnerId ownerId, Money initialBalance) {
        this(UUID.randomUUID(), LocalDateTime.now(), walletId, ownerId, initialBalance);
    }
}
