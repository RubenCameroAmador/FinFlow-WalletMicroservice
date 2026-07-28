package com.rubencamero.finflow.domain.entity;

import com.rubencamero.finflow.domain.event.DomainEvent;
import com.rubencamero.finflow.domain.event.MoneyDeposited;
import com.rubencamero.finflow.domain.event.MoneyWithdrawn;
import com.rubencamero.finflow.domain.event.WalletActivated;
import com.rubencamero.finflow.domain.event.WalletCreated;
import com.rubencamero.finflow.domain.event.WalletFrozen;
import com.rubencamero.finflow.domain.exception.InvalidWalletException;
import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletTest {

    private final Currency usd = Currency.getInstance("USD");

    private Wallet activeWallet(String initialAmount) {
        Wallet wallet = new Wallet(OwnerId.generate(), new Money(usd, new BigDecimal(initialAmount)));
        wallet.pullEvents(); // discard the WalletCreated event so later assertions start from a clean slate
        return wallet;
    }

    // --- creation ---

    @Test
    void shouldCreateActiveWalletWithInitialBalance() {
        OwnerId ownerId = OwnerId.generate();
        Money initialBalance = new Money(usd, new BigDecimal("100.00"));

        Wallet wallet = new Wallet(ownerId, initialBalance);

        assertThat(wallet.getOwnerId()).isEqualTo(ownerId);
        assertThat(wallet.getBalance()).isEqualTo(initialBalance);
        assertThat(wallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
        assertThat(wallet.getId()).isNotNull();
        assertThat(wallet.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldRaiseWalletCreatedEventOnCreation() {
        OwnerId ownerId = OwnerId.generate();
        Money initialBalance = new Money(usd, new BigDecimal("100.00"));

        Wallet wallet = new Wallet(ownerId, initialBalance);
        List<DomainEvent> events = wallet.pullEvents();

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(WalletCreated.class);

        WalletCreated event = (WalletCreated) events.get(0);
        assertThat(event.walletId()).isEqualTo(wallet.getId());
        assertThat(event.ownerId()).isEqualTo(ownerId);
        assertThat(event.initialBalance()).isEqualTo(initialBalance);
    }

    @Test
    void pullEventsShouldClearEventsAfterReading() {
        Wallet wallet = activeWallet("10.00");

        wallet.pullEvents();
        List<DomainEvent> secondRead = wallet.pullEvents();

        assertThat(secondRead).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenOwnerIdIsNull() {
        Money initialBalance = new Money(usd, new BigDecimal("10.00"));

        assertThatThrownBy(() -> new Wallet(null, initialBalance))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowExceptionWhenInitialBalanceIsNull() {
        assertThatThrownBy(() -> new Wallet(OwnerId.generate(), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotBeAbleToConstructNegativeInitialBalanceBecauseMoneyAlreadyBlocksIt() {
        assertThatThrownBy(() -> new Money(usd, new BigDecimal("-10.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // --- deposit ---

    @Test
    void depositShouldIncreaseBalance() {
        Wallet wallet = activeWallet("100.00");
        Money depositAmount = new Money(usd, new BigDecimal("50.00"));

        wallet.deposit(depositAmount);

        assertThat(wallet.getBalance().amount()).isEqualByComparingTo("150.00");
    }

    @Test
    void depositShouldRaiseMoneyDepositedEventWithPreviousAndDepositedAmounts() {
        Wallet wallet = activeWallet("100.00");
        Money balanceBeforeDeposit = wallet.getBalance();
        Money depositAmount = new Money(usd, new BigDecimal("50.00"));

        wallet.deposit(depositAmount);
        List<DomainEvent> events = wallet.pullEvents();

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(MoneyDeposited.class);

        MoneyDeposited event = (MoneyDeposited) events.get(0);
        assertThat(event.previousAmount()).isEqualTo(balanceBeforeDeposit);
        assertThat(event.depositedAmount()).isEqualTo(depositAmount);
    }

    @Test
    void depositOnFrozenWalletShouldThrowException() {
        Wallet wallet = activeWallet("100.00");
        wallet.freeze();
        Money depositAmount = new Money(usd, new BigDecimal("10.00"));

        assertThatThrownBy(() -> wallet.deposit(depositAmount))
                .isInstanceOf(InvalidWalletException.class);
    }

    // --- withdraw ---

    @Test
    void withdrawShouldDecreaseBalance() {
        Wallet wallet = activeWallet("100.00");
        Money withdrawAmount = new Money(usd, new BigDecimal("40.00"));

        wallet.withdraw(withdrawAmount);

        assertThat(wallet.getBalance().amount()).isEqualByComparingTo("60.00");
    }

    @Test
    void withdrawShouldRaiseMoneyWithdrawnEventWithPreviousAndWithdrawnAmounts() {
        Wallet wallet = activeWallet("100.00");
        Money balanceBeforeWithdraw = wallet.getBalance();
        Money withdrawAmount = new Money(usd, new BigDecimal("40.00"));

        wallet.withdraw(withdrawAmount);
        List<DomainEvent> events = wallet.pullEvents();

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(MoneyWithdrawn.class);

        MoneyWithdrawn event = (MoneyWithdrawn) events.get(0);
        assertThat(event.previousAmount()).isEqualTo(balanceBeforeWithdraw);
        assertThat(event.withDrawnAmount()).isEqualTo(withdrawAmount);
    }

    @Test
    void withdrawOnFrozenWalletShouldThrowException() {
        Wallet wallet = activeWallet("100.00");
        wallet.freeze();
        Money withdrawAmount = new Money(usd, new BigDecimal("10.00"));

        assertThatThrownBy(() -> wallet.withdraw(withdrawAmount))
                .isInstanceOf(InvalidWalletException.class);
    }

    @Test
    void withdrawWithInsufficientBalanceShouldThrowInvalidWalletException() {
        Wallet wallet = activeWallet("30.00");
        Money withdrawAmount = new Money(usd, new BigDecimal("50.00"));

        assertThatThrownBy(() -> wallet.withdraw(withdrawAmount))
                .isInstanceOf(InvalidWalletException.class)
                .hasMessage("Insufficient balance.");
    }

    // --- freeze / activate ---

    @Test
    void freezeShouldChangeStatusToFrozenAndRaiseEvent() {
        Wallet wallet = activeWallet("10.00");

        wallet.freeze();
        List<DomainEvent> events = wallet.pullEvents();

        assertThat(wallet.getStatus()).isEqualTo(WalletStatus.FROZEN);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(WalletFrozen.class);
    }

    @Test
    void freezeAlreadyFrozenWalletShouldThrowException() {
        Wallet wallet = activeWallet("10.00");
        wallet.freeze();

        assertThatThrownBy(wallet::freeze)
                .isInstanceOf(InvalidWalletException.class);
    }

    @Test
    void activateShouldChangeStatusToActiveAndRaiseEvent() {
        Wallet wallet = activeWallet("10.00");
        wallet.freeze();
        wallet.pullEvents();

        wallet.activate();
        List<DomainEvent> events = wallet.pullEvents();

        assertThat(wallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(WalletActivated.class);
    }

    @Test
    void activateAlreadyActiveWalletShouldThrowException() {
        Wallet wallet = activeWallet("10.00");

        assertThatThrownBy(wallet::activate)
                .isInstanceOf(InvalidWalletException.class);
    }
}
