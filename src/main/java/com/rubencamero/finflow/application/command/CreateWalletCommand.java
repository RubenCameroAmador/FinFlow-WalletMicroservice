package com.rubencamero.finflow.application.command;

import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletStatus;

import java.util.Objects;

public record CreateWalletCommand (
        OwnerId ownerId,
        Money initialBalance,
        WalletStatus status
) {
    public CreateWalletCommand {
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(initialBalance);
        Objects.requireNonNull(status);
    }
}
