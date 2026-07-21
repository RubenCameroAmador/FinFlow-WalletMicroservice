package com.rubencamero.finflow.application.command;

import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletId;

import java.util.Objects;

public record DepositMoneyCommand(
        WalletId walletId,
        Money amount
) {
    public DepositMoneyCommand {
        Objects.requireNonNull(walletId);
        Objects.requireNonNull(amount);
    }
}
