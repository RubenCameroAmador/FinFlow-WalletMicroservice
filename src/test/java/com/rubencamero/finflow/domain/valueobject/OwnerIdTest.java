package com.rubencamero.finflow.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OwnerIdTest {

    @Test
    void generateShouldCreateNonNullId() {
        OwnerId id = OwnerId.generate();

        assertThat(id).isNotNull();
        assertThat(id.value()).isNotNull();
    }

    @Test
    void generateShouldCreateDifferentIdsOnEachCall() {
        assertThat(OwnerId.generate()).isNotEqualTo(OwnerId.generate());
    }

    @Test
    void ofShouldWrapGivenUuid() {
        UUID uuid = UUID.randomUUID();

        OwnerId id = OwnerId.of(uuid);

        assertThat(id.value()).isEqualTo(uuid);
    }

    @Test
    void ofShouldThrowExceptionWhenUuidIsNull() {
        assertThatThrownBy(() -> OwnerId.of(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void equalsShouldBeBasedOnUnderlyingUuidNotReference() {
        UUID uuid = UUID.randomUUID();

        OwnerId a = OwnerId.of(uuid);
        OwnerId b = OwnerId.of(uuid);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotSameAs(b);
    }

    @Test
    void hashCodeShouldBeConsistentWithEquals() {
        UUID uuid = UUID.randomUUID();

        OwnerId a = OwnerId.of(uuid);
        OwnerId b = OwnerId.of(uuid);

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void equalsShouldReturnFalseForDifferentUuid() {
        OwnerId a = OwnerId.of(UUID.randomUUID());
        OwnerId b = OwnerId.of(UUID.randomUUID());

        assertThat(a).isNotEqualTo(b);
    }
}
