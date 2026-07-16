package com.rubencamero.finflow.application.command;

import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.UserId;

import java.util.Objects;

public record CreateWalletCommand (
        UserId ownerId,
        Money initialBalance
) {
    public CreateWalletCommand {
        Objects.requireNonNull(ownerId);
        Objects.requireNonNull(initialBalance);
    }
}
