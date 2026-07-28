package com.rubencamero.finflow.application.usecase;

import com.rubencamero.finflow.application.command.ActivateWalletCommand;
import com.rubencamero.finflow.application.port.DomainEventPublisher;
import com.rubencamero.finflow.application.port.WalletRepository;
import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.exception.InvalidWalletException;
import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletId;
import com.rubencamero.finflow.domain.valueobject.WalletStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;

@SpringBootTest
@Transactional
class ActivateWalletUseCaseIntegrationTest {

    @Autowired
    private ActivateWalletUseCase activateWalletUseCase;

    @Autowired
    private WalletRepository walletRepository;

    @MockitoBean
    private DomainEventPublisher domainEventPublisher;

    private Wallet persistedFrozenWallet() {
        Wallet wallet = new Wallet(OwnerId.generate(), new Money(Currency.getInstance("USD"), new BigDecimal("100.00")));
        wallet.freeze();
        return walletRepository.save(wallet);
    }

    @Test
    void shouldActivateAFrozenWalletAndPersistIt() {
        Wallet wallet = persistedFrozenWallet();
        ActivateWalletCommand command = new ActivateWalletCommand(wallet.getId());

        activateWalletUseCase.execute(command);

        Wallet updated = walletRepository.findById(wallet.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(WalletStatus.ACTIVE);
        verify(domainEventPublisher).publish(anyList());
    }

    @Test
    void shouldThrowExceptionWhenWalletDoesNotExist() {
        ActivateWalletCommand command = new ActivateWalletCommand(WalletId.generate());

        assertThatThrownBy(() -> activateWalletUseCase.execute(command))
                .isInstanceOf(InvalidWalletException.class);
    }

    @Test
    void shouldThrowExceptionWhenWalletIsAlreadyActive() {
        Wallet wallet = new Wallet(OwnerId.generate(), new Money(Currency.getInstance("USD"), new BigDecimal("100.00")));
        walletRepository.save(wallet);

        ActivateWalletCommand command = new ActivateWalletCommand(wallet.getId());

        assertThatThrownBy(() -> activateWalletUseCase.execute(command))
                .isInstanceOf(InvalidWalletException.class);
    }
}
