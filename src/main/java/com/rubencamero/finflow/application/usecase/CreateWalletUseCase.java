package com.rubencamero.finflow.application.usecase;

import com.rubencamero.finflow.application.command.CreateWalletCommand;
import com.rubencamero.finflow.application.port.DomainEventPublisher;
import com.rubencamero.finflow.application.port.WalletRepository;
import com.rubencamero.finflow.domain.entity.Wallet;
import org.springframework.stereotype.Service;

@Service
public class CreateWalletUseCase {
   private final WalletRepository walletRepository;
   private final DomainEventPublisher domainEventPublisher;

   public CreateWalletUseCase(WalletRepository walletRepository, DomainEventPublisher domainEventPublisher){
       this.walletRepository = walletRepository;
       this.domainEventPublisher = domainEventPublisher;
   }

   public Wallet execute(CreateWalletCommand command){
        Wallet wallet = new Wallet(
                command.ownerId(),
                command.initialBalance(),
                command.status()
        );

        Wallet saved = walletRepository.save(wallet);

        domainEventPublisher.publish(wallet.pullEvents());

        return saved;
   }
}
