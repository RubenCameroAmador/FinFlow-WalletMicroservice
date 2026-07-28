package com.rubencamero.finflow.infrastructure.api.mapper;

import com.rubencamero.finflow.application.command.CreateWalletCommand;
import com.rubencamero.finflow.application.command.DepositMoneyCommand;
import com.rubencamero.finflow.application.command.WithdrawMoneyCommand;
import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletId;
import com.rubencamero.finflow.infrastructure.api.dto.CreateWalletRequest;
import com.rubencamero.finflow.infrastructure.api.dto.DepositRequest;
import com.rubencamero.finflow.infrastructure.api.dto.WithdrawRequest;
import com.rubencamero.finflow.infrastructure.api.dto.WalletResponse;

import java.util.Currency;
import java.util.UUID;

public class WalletApiMapper {

    // DTO → Command (para crear wallet)
    public static CreateWalletCommand toCommand(CreateWalletRequest request) {
        OwnerId ownerId = OwnerId.of(UUID.fromString(request.ownerId()));
        Money initialBalance = new Money(
                Currency.getInstance(request.currency()),
                request.amount()
        );

        return new CreateWalletCommand(ownerId, initialBalance);
    }

    // DTO → Command (para depositar)
    public static DepositMoneyCommand toCommand(DepositRequest request, WalletId walletId) {
        Money amount = new Money(
                Currency.getInstance(request.currency()),
                request.amount()
        );
        return new DepositMoneyCommand(walletId, amount);
    }

    // DTO → Command (para retirar)
    public static WithdrawMoneyCommand toCommand(WithdrawRequest request, WalletId walletId) {
        Money amount = new Money(
                Currency.getInstance(request.currency()),
                request.amount()
        );
        return new WithdrawMoneyCommand(walletId, amount);
    }

    // Wallet → DTO (para responder)
    public static WalletResponse toResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.getId().value().toString(),
                wallet.getOwnerId().value().toString(),
                wallet.getBalance().amount(),
                wallet.getBalance().currency().getCurrencyCode(),
                wallet.getStatus().name(),
                wallet.getCreatedAt()
        );
    }
}
