package com.rubencamero.finflow.domain.event;

import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletId;
import com.rubencamero.finflow.domain.valueobject.WalletStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record WalletFrozen(
        UUID eventId,
        LocalDateTime occurredOn,
        WalletId walletId,
        OwnerId ownerId
) implements DomainEvent {
    public WalletFrozen(WalletId walletId, OwnerId ownerId){
        this(UUID.randomUUID(), LocalDateTime.now(), walletId, ownerId);
    }
}
