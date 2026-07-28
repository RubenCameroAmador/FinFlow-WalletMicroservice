package com.rubencamero.finflow.infrastructure.messaging.mapper;

import com.rubencamero.finflow.domain.event.*;
import com.rubencamero.finflow.infrastructure.messaging.event.*;

public class WalletEventMapper {

    public static WalletIntegrationEvent toIntegrationEvent(DomainEvent event) {
        return switch (event) {
            case WalletCreated e -> new WalletCreatedEvent(
                    e.eventId(), e.occurredOn(),
                    e.walletId().value(), e.ownerId().value(),
                    e.initialBalance().amount(), e.initialBalance().currency().getCurrencyCode()
            );
            case WalletActivated e -> new WalletActivatedEvent(
                    e.eventId(), e.occurredOn(), e.walletId().value(), e.ownerId().value()
            );
            case WalletFrozen e -> new WalletFrozenEvent(
                    e.eventId(), e.occurredOn(), e.walletId().value(), e.ownerId().value()
            );
            case MoneyDeposited e -> new MoneyDepositedEvent(
                    e.eventId(), e.occurredOn(), e.walletId().value(), e.ownerId().value(),
                    e.previousAmount().amount(), e.depositedAmount().amount(),
                    e.depositedAmount().currency().getCurrencyCode()
            );
            case MoneyWithdrawn e -> new MoneyWithdrawnEvent(
                    e.eventId(), e.occurredOn(), e.walletId().value(), e.ownerId().value(),
                    e.previousAmount().amount(), e.withDrawnAmount().amount(),
                    e.withDrawnAmount().currency().getCurrencyCode()
            );
        };
    }
}