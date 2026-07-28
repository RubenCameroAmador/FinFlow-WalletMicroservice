package com.rubencamero.finflow.application.usecase;

import com.rubencamero.finflow.application.command.GetWalletQuery;
import com.rubencamero.finflow.application.port.WalletRepository;
import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.exception.InvalidWalletException;
import org.springframework.stereotype.Service;

@Service
public class GetWalletUseCase {
    private final WalletRepository walletRepository;

    public GetWalletUseCase(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Wallet execute(GetWalletQuery query) {
        return walletRepository.findById(query.walletId())
                .orElseThrow(() -> new InvalidWalletException("Wallet not found"));
    }
}
