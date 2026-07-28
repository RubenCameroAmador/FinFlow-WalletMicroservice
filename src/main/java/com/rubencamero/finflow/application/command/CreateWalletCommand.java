package com.rubencamero.finflow.application.command;

import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;

import java.util.Objects;

public record CreateWalletCommand (
        OwnerId ownerId,
        Money initialBalance
) {
    public CreateWalletCommand {
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(initialBalance);
    }
}
