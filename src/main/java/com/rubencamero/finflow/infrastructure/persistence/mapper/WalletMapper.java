package com.rubencamero.finflow.infrastructure.persistence.mapper;

import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletId;
import com.rubencamero.finflow.infrastructure.persistence.entity.WalletEntity;

import java.util.Currency;

public class WalletMapper {

    public static WalletEntity toEntity(Wallet wallet) {
        return WalletEntity.builder()
                .walletId(wallet.getId().value())
                .owerId(wallet.getOwnerId().value())
                .amount(wallet.getBalance().amount())
                .currency(wallet.getBalance().currency().getCurrencyCode())
                .status(wallet.getStatus())
                .createdAt(wallet.getCreatedAt())
                .build();
    }

    public static Wallet toDomain(WalletEntity entity) {
        Money balance = new Money(
                Currency.getInstance(entity.getCurrency()),
                entity.getAmount()
        );

        return Wallet.reconstitute(
                WalletId.of(entity.getWalletId()),
                OwnerId.of(entity.getOwerId()),
                balance,
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
