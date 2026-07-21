package com.rubencamero.finflow.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public final class OwnerId {
    private final UUID value;

    private OwnerId(UUID value) {
        this.value = Objects.requireNonNull(value);
    }

    public static OwnerId generate() {
        return new OwnerId(UUID.randomUUID());
    }

    public static OwnerId of(UUID value) {
        return new OwnerId(value);
    }

    public UUID value() {
        return value;
    }
}
