package com.rubencamero.finflow.infrastructure.persistence.repository;

import com.rubencamero.finflow.application.port.WalletRepository;
import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.valueobject.WalletId;
import com.rubencamero.finflow.infrastructure.persistence.entity.WalletEntity;
import com.rubencamero.finflow.infrastructure.persistence.mapper.WalletMapper;

import java.util.Optional;

public class PostgresWalletRepository implements WalletRepository {
    private final WalletJpaRepository walletJpaRepository;

    public PostgresWalletRepository(WalletJpaRepository walletJpaRepository) {
        this.walletJpaRepository = walletJpaRepository;
    }

    @Override
    public Wallet save(Wallet wallet) {
        WalletEntity walletEntity = WalletMapper.toEntity(wallet);
        WalletEntity saved = walletJpaRepository.save(walletEntity);
        return WalletMapper.toDomain(saved);
    }

    @Override
    public Optional<Wallet> findById(WalletId id) {
        return walletJpaRepository.findById(id.value()).map(WalletMapper::toDomain);
    }
}
