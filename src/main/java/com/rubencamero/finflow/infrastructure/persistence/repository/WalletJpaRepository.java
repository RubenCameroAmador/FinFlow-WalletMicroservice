package com.rubencamero.finflow.infrastructure.persistence.repository;

import com.rubencamero.finflow.infrastructure.persistence.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WalletJpaRepository extends JpaRepository<WalletEntity, UUID> {
}
