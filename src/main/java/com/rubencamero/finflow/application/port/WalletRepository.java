package com.rubencamero.finflow.application.port;

import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.valueobject.WalletId;

import java.util.Optional;

public interface WalletRepository {

    Wallet save(Wallet wallet);
    Optional<Wallet> findById(WalletId id);
}
