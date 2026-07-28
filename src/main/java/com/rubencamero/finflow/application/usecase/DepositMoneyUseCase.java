package com.rubencamero.finflow.application.usecase;

import com.rubencamero.finflow.application.command.DepositMoneyCommand;
import com.rubencamero.finflow.application.port.DomainEventPublisher;
import com.rubencamero.finflow.application.port.WalletRepository;
import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.exception.InvalidWalletException;
import org.springframework.stereotype.Service;

@Service
public class DepositMoneyUseCase {
    private final WalletRepository walletRepository;
    private final DomainEventPublisher domainEventPublisher;

    public DepositMoneyUseCase(WalletRepository walletRepository, DomainEventPublisher domainEventPublisher) {
        this.walletRepository = walletRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public Wallet execute(DepositMoneyCommand command) {
        Wallet wallet = walletRepository.findById(command.walletId())
                .orElseThrow(() -> new InvalidWalletException("Wallet not found"));

        wallet.deposit(command.amount());

        Wallet saved = walletRepository.save(wallet);

        domainEventPublisher.publish(wallet.pullEvents());

        return saved;
    }
}
