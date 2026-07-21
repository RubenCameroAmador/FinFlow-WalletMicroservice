package com.rubencamero.finflow.application.command;

import com.rubencamero.finflow.domain.valueobject.WalletId;

import java.util.Objects;

public record ActivateWalletCommand(
        WalletId walletId
) {
    public ActivateWalletCommand {
        Objects.requireNonNull(walletId);
    }
}
