package com.rubencamero.finflow.domain.event;

import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletId;

import java.time.LocalDateTime;
import java.util.UUID;

public record MoneyWithdrawn(
        UUID eventId,
        LocalDateTime occurredOn,
        WalletId walletId,
        OwnerId ownerId,
        Money previousAmount,
        Money withDrawnAmount
) implements DomainEvent {
    public MoneyWithdrawn( WalletId walletId, OwnerId ownerId, Money previousAmount ,Money withDrawnAmount){
        this(UUID.randomUUID(),
                LocalDateTime.now(),
                walletId,
                ownerId,
                previousAmount,
                withDrawnAmount);
    }
}
