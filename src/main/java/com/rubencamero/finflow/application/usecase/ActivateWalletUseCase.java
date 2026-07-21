package com.rubencamero.finflow.application.usecase;

import com.rubencamero.finflow.application.command.ActivateWalletCommand;
import com.rubencamero.finflow.application.port.DomainEventPublisher;
import com.rubencamero.finflow.application.port.WalletRepository;
import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.exception.InvalidWalletException;

public class ActivateWalletUseCase {
    private final WalletRepository walletRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ActivateWalletUseCase(WalletRepository walletRepository, DomainEventPublisher domainEventPublisher) {
        this.walletRepository = walletRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public Wallet execute(ActivateWalletCommand command) {
        Wallet wallet = walletRepository.findById(command.walletId())
                .orElseThrow(() -> new InvalidWalletException("Wallet not found"));

        wallet.activate();

        Wallet saved = walletRepository.save(wallet);

        domainEventPublisher.publish(wallet.pullEvents());

        return saved;
    }
}
