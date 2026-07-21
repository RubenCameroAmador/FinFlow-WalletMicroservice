package com.rubencamero.finflow.application.command;

import com.rubencamero.finflow.domain.valueobject.WalletId;

import java.util.Objects;

public record FreezeWalletCommand(
        WalletId walletId
) {
    public FreezeWalletCommand{
        Objects.requireNonNull(walletId);
    }
}
