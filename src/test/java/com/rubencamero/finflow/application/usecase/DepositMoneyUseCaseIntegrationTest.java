package com.rubencamero.finflow.application.usecase;

import com.rubencamero.finflow.application.command.DepositMoneyCommand;
import com.rubencamero.finflow.application.port.DomainEventPublisher;
import com.rubencamero.finflow.application.port.WalletRepository;
import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.exception.InvalidWalletException;
import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletId;
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
class DepositMoneyUseCaseIntegrationTest {

    @Autowired
    private DepositMoneyUseCase depositMoneyUseCase;

    @Autowired
    private WalletRepository walletRepository;

    @MockitoBean
    private DomainEventPublisher domainEventPublisher;

    private Wallet persistedWallet(String initialAmount) {
        Wallet wallet = new Wallet(OwnerId.generate(), new Money(Currency.getInstance("USD"), new BigDecimal(initialAmount)));
        return walletRepository.save(wallet);
    }

    @Test
    void shouldIncreaseWalletBalanceAndPersistIt() {
        Wallet wallet = persistedWallet("100.00");
        DepositMoneyCommand command = new DepositMoneyCommand(
                wallet.getId(), new Money(Currency.getInstance("USD"), new BigDecimal("50.00")));

        depositMoneyUseCase.execute(command);

        Wallet updated = walletRepository.findById(wallet.getId()).orElseThrow();
        assertThat(updated.getBalance().amount()).isEqualByComparingTo("150.00");
        verify(domainEventPublisher).publish(anyList());
    }

    @Test
    void shouldThrowExceptionWhenWalletDoesNotExist() {
        DepositMoneyCommand command = new DepositMoneyCommand(
                WalletId.generate(), new Money(Currency.getInstance("USD"), new BigDecimal("50.00")));

        assertThatThrownBy(() -> depositMoneyUseCase.execute(command))
                .isInstanceOf(InvalidWalletException.class);
    }

    @Test
    void shouldThrowExceptionWhenWalletIsFrozen() {
        Wallet wallet = persistedWallet("100.00");
        Wallet frozen = walletRepository.findById(wallet.getId()).orElseThrow();
        frozen.freeze();
        walletRepository.save(frozen);

        DepositMoneyCommand command = new DepositMoneyCommand(
                wallet.getId(), new Money(Currency.getInstance("USD"), new BigDecimal("10.00")));

        assertThatThrownBy(() -> depositMoneyUseCase.execute(command))
                .isInstanceOf(InvalidWalletException.class);
    }
}
