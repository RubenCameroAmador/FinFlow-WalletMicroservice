package com.rubencamero.finflow.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public class WalletId {
    private final UUID value;

    private WalletId(UUID value) {
        this.value = Objects.requireNonNull(value);
    }

    public static WalletId generate() {
        return new WalletId(UUID.randomUUID());
    }

    public static WalletId of(UUID value) {
        return new WalletId(value);
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        WalletId walletId = (WalletId) other;
        return Objects.equals(value, walletId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
