package com.rubencamero.finflow.application.usecase;

import com.rubencamero.finflow.application.command.FreezeWalletCommand;
import com.rubencamero.finflow.application.port.DomainEventPublisher;
import com.rubencamero.finflow.application.port.WalletRepository;
import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.exception.InvalidWalletException;

public class FreezeWalletUseCase {
    private final WalletRepository walletRepository;
    private final DomainEventPublisher domainEventPublisher;

    public FreezeWalletUseCase(WalletRepository walletRepository, DomainEventPublisher domainEventPublisher) {
        this.walletRepository = walletRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public Wallet execute(FreezeWalletCommand command){
        Wallet wallet = walletRepository.findById(command.walletId())
                .orElseThrow( () -> new InvalidWalletException("Wallet not found"));

        wallet.freeze();

        Wallet saved = walletRepository.save(wallet);

        domainEventPublisher.publish(wallet.pullEvents());

        return saved;
    }
}
