package com.rubencamero.finflow.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletIdTest {

    @Test
    void generateShouldCreateNonNullId() {
        WalletId id = WalletId.generate();

        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }

    @Test
    void generateShouldCreateDifferentIdsOnEachCall() {
        assertThat(WalletId.generate()).isNotEqualTo(WalletId.generate());
    }

    @Test
    void ofShouldWrapGivenUuid() {
        UUID uuid = UUID.randomUUID();

        WalletId id = WalletId.of(uuid);

        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    void ofShouldThrowExceptionWhenUuidIsNull() {
        assertThatThrownBy(() -> WalletId.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equalsShouldBeBasedOnUnderlyingUuidNotReference() {
        UUID uuid = UUID.randomUUID();

        WalletId a = WalletId.of(uuid);
        WalletId b = WalletId.of(uuid);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotSameAs(b);
    }

    @Test
    void hashCodeShouldBeConsistentWithEquals() {
        UUID uuid = UUID.randomUUID();

        WalletId a = WalletId.of(uuid);
        WalletId b = WalletId.of(uuid);

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void equalsShouldReturnFalseForDifferentUuid() {
        WalletId a = WalletId.of(UUID.randomUUID());
        WalletId b = WalletId.of(UUID.randomUUID());

        assertThat(a).isNotEqualTo(b);
    }
}
