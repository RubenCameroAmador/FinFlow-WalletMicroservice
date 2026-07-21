package com.rubencamero.finflow.domain.event;

import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletId;

import java.time.LocalDateTime;
import java.util.UUID;

public record WalletActivated(UUID eventId,
                              LocalDateTime occurredOn,
                              WalletId walletId,
                              OwnerId ownerId) implements DomainEvent{
    public WalletActivated(WalletId walletId, OwnerId ownerId){
        this(UUID.randomUUID(), LocalDateTime.now(), walletId, ownerId);
    }
}
