package com.rubencamero.finflow.domain.event;

import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletId;

import java.time.LocalDateTime;
import java.util.UUID;

public record MoneyDeposited(
        UUID eventId,
        LocalDateTime occurredOn,
        WalletId walletId,
        OwnerId ownerId,
        Money previousAmount,
        Money depositedAmount
) implements DomainEvent {
    public MoneyDeposited( WalletId walletId, OwnerId ownerId, Money previousAmount ,Money depositedAmount){
        this(UUID.randomUUID(),
                LocalDateTime.now(),
                walletId,
                ownerId,
                previousAmount,
                depositedAmount);
    }
}
