package com.rubencamero.finflow.application.command;

import com.rubencamero.finflow.domain.valueobject.WalletId;

import java.util.Objects;

public record GetWalletQuery(
        WalletId walletId
) {
    public GetWalletQuery {
        Objects.requireNonNull(walletId);
    }
}
