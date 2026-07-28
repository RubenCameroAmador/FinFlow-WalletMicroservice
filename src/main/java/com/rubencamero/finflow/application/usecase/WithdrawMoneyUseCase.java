package com.rubencamero.finflow.application.usecase;

import com.rubencamero.finflow.application.command.WithdrawMoneyCommand;
import com.rubencamero.finflow.application.port.DomainEventPublisher;
import com.rubencamero.finflow.application.port.WalletRepository;
import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.exception.InvalidWalletException;
import org.springframework.stereotype.Service;

@Service
public class WithdrawMoneyUseCase {
    private final WalletRepository walletRepository;
    private final DomainEventPublisher domainEventPublisher;

    public WithdrawMoneyUseCase(WalletRepository walletRepository, DomainEventPublisher domainEventPublisher) {
        this.walletRepository = walletRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public Wallet execute(WithdrawMoneyCommand command){
        Wallet wallet = walletRepository.findById(command.walletId())
                .orElseThrow(() -> new InvalidWalletException("Wallet not found"));

        wallet.withdraw(command.amount());

        Wallet saved = walletRepository.save(wallet);

        domainEventPublisher.publish(wallet.pullEvents());
        return saved;
    }
}
