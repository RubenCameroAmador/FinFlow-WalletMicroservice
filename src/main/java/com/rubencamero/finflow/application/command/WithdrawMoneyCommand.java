package com.rubencamero.finflow.application.command;

import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.WalletId;

import java.util.Objects;
import java.util.UUID;

public record WithdrawMoneyCommand(
        WalletId walletId,
        Money amount
) {
    public WithdrawMoneyCommand{
        Objects.requireNonNull(walletId);
        Objects.requireNonNull(amount);
    }
}
