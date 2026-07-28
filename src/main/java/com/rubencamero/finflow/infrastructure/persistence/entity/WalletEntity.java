package com.rubencamero.finflow.infrastructure.persistence.entity;

import com.rubencamero.finflow.domain.valueobject.WalletStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletEntity {
    @Id
    private UUID walletId;

    private UUID ownerId;
    private String currency;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private WalletStatus status;

    private LocalDateTime createdAt;

}
