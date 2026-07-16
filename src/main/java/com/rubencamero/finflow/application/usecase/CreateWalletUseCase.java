package com.rubencamero.finflow.application.usecase;

import com.rubencamero.finflow.application.command.CreateWalletCommand;
import com.rubencamero.finflow.application.port.WalletRepository;
import com.rubencamero.finflow.domain.entity.Wallet;

public class CreateWalletUseCase {
   private final WalletRepository walletRepository;

   public CreateWalletUseCase(WalletRepository walletRepository){
       this.walletRepository = walletRepository;
   }

   public Wallet execute(CreateWalletCommand command){
        Wallet wallet = new Wallet(
                command.ownerId(),
                command.initialBalance()
        );
        return walletRepository.save(wallet);
   }
}
