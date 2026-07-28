package com.rubencamero.finflow.application.usecase;

import com.rubencamero.finflow.application.command.FreezeWalletCommand;
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
class FreezeWalletUseCaseIntegrationTest {

    @Autowired
    private FreezeWalletUseCase freezeWalletUseCase;

    @Autowired
    private WalletRepository walletRepository;

    @MockitoBean
    private DomainEventPublisher domainEventPublisher;

    private Wallet persistedWallet() {
        Wallet wallet = new Wallet(OwnerId.generate(), new Money(Currency.getInstance("USD"), new BigDecimal("100.00")));
        return walletRepository.save(wallet);
    }

    @Test
    void shouldFreezeAnActiveWalletAndPersistIt() {
        Wallet wallet = persistedWallet();
        FreezeWalletCommand command = new FreezeWalletCommand(wallet.getId());

        freezeWalletUseCase.execute(command);

        Wallet updated = walletRepository.findById(wallet.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(WalletStatus.FROZEN);
        verify(domainEventPublisher).publish(anyList());
    }

    @Test
    void shouldThrowExceptionWhenWalletDoesNotExist() {
        FreezeWalletCommand command = new FreezeWalletCommand(WalletId.generate());

        assertThatThrownBy(() -> freezeWalletUseCase.execute(command))
                .isInstanceOf(InvalidWalletException.class);
    }

    @Test
    void shouldThrowExceptionWhenWalletIsAlreadyFrozen() {
        Wallet wallet = persistedWallet();
        Wallet frozen = walletRepository.findById(wallet.getId()).orElseThrow();
        frozen.freeze();
        walletRepository.save(frozen);

        FreezeWalletCommand command = new FreezeWalletCommand(wallet.getId());

        assertThatThrownBy(() -> freezeWalletUseCase.execute(command))
                .isInstanceOf(InvalidWalletException.class);
    }
}
