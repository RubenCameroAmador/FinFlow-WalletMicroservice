package com.rubencamero.finflow.bdd;

import com.rubencamero.finflow.application.command.ActivateWalletCommand;
import com.rubencamero.finflow.application.command.CreateWalletCommand;
import com.rubencamero.finflow.application.command.DepositMoneyCommand;
import com.rubencamero.finflow.application.command.FreezeWalletCommand;
import com.rubencamero.finflow.application.command.WithdrawMoneyCommand;
import com.rubencamero.finflow.application.usecase.ActivateWalletUseCase;
import com.rubencamero.finflow.application.usecase.CreateWalletUseCase;
import com.rubencamero.finflow.application.usecase.DepositMoneyUseCase;
import com.rubencamero.finflow.application.usecase.FreezeWalletUseCase;
import com.rubencamero.finflow.application.usecase.WithdrawMoneyUseCase;
import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.exception.InvalidWalletException;
import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletStatus;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @Transactional here (Spring's, not the use cases' own) rolls back everything this scenario
 * wrote to Postgres once the scenario ends - same trick used in the *UseCaseIntegrationTest classes.
 */
@Transactional
public class WalletStepDefinitions {

    private final CreateWalletUseCase createWalletUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;
    private final FreezeWalletUseCase freezeWalletUseCase;
    private final ActivateWalletUseCase activateWalletUseCase;

    private Money pendingInitialBalance;
    private Wallet wallet;
    private InvalidWalletException caughtException;

    public WalletStepDefinitions(CreateWalletUseCase createWalletUseCase,
                                  DepositMoneyUseCase depositMoneyUseCase,
                                  WithdrawMoneyUseCase withdrawMoneyUseCase,
                                  FreezeWalletUseCase freezeWalletUseCase,
                                  ActivateWalletUseCase activateWalletUseCase) {
        this.createWalletUseCase = createWalletUseCase;
        this.depositMoneyUseCase = depositMoneyUseCase;
        this.withdrawMoneyUseCase = withdrawMoneyUseCase;
        this.freezeWalletUseCase = freezeWalletUseCase;
        this.activateWalletUseCase = activateWalletUseCase;
    }

    private Money money(double amount, String currencyCode) {
        return new Money(Currency.getInstance(currencyCode), BigDecimal.valueOf(amount));
    }

    // --- Given ---

    @Given("an owner wants to create a wallet with an initial balance of {double} {word}")
    public void anOwnerWantsToCreateAWalletWithAnInitialBalanceOf(double amount, String currency) {
        this.pendingInitialBalance = money(amount, currency);
    }

    @Given("an active wallet with a balance of {double} {word}")
    public void anActiveWalletWithABalanceOf(double amount, String currency) {
        this.wallet = createWalletUseCase.execute(
                new CreateWalletCommand(OwnerId.generate(), money(amount, currency)));
    }

    @Given("a frozen wallet with a balance of {double} {word}")
    public void aFrozenWalletWithABalanceOf(double amount, String currency) {
        anActiveWalletWithABalanceOf(amount, currency);
        this.wallet = freezeWalletUseCase.execute(new FreezeWalletCommand(wallet.getId()));
    }

    // --- When ---

    @When("the wallet is created")
    public void theWalletIsCreated() {
        this.wallet = createWalletUseCase.execute(new CreateWalletCommand(OwnerId.generate(), pendingInitialBalance));
    }

    @When("{double} {word} is deposited into the wallet")
    public void amountIsDepositedIntoTheWallet(double amount, String currency) {
        try {
            this.wallet = depositMoneyUseCase.execute(new DepositMoneyCommand(wallet.getId(), money(amount, currency)));
        } catch (InvalidWalletException exception) {
            this.caughtException = exception;
        }
    }

    @When("{double} {word} is withdrawn from the wallet")
    public void amountIsWithdrawnFromTheWallet(double amount, String currency) {
        try {
            this.wallet = withdrawMoneyUseCase.execute(new WithdrawMoneyCommand(wallet.getId(), money(amount, currency)));
        } catch (InvalidWalletException exception) {
            this.caughtException = exception;
        }
    }

    @When("the wallet is frozen")
    public void theWalletIsFrozen() {
        this.wallet = freezeWalletUseCase.execute(new FreezeWalletCommand(wallet.getId()));
    }

    @When("the wallet is activated")
    public void theWalletIsActivated() {
        this.wallet = activateWalletUseCase.execute(new ActivateWalletCommand(wallet.getId()));
    }

    // --- Then ---

    @Then("the wallet should be active")
    public void theWalletShouldBeActive() {
        assertThat(wallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
    }

    @Then("the wallet should be frozen")
    public void theWalletShouldBeFrozen() {
        assertThat(wallet.getStatus()).isEqualTo(WalletStatus.FROZEN);
    }

    @Then("the wallet balance should be {double} {word}")
    public void theWalletBalanceShouldBe(double amount, String currency) {
        Money expected = money(amount, currency);
        // Comparing by value (compareTo), not by representation (equals): the balance we read
        // back after a findById() round-trip through Postgres can come back with a different
        // BigDecimal scale (e.g. "150.00" vs the in-memory "150.0"), and Money.equals()/BigDecimal.equals()
        // is scale-sensitive even though the two numbers are the same amount.
        assertThat(wallet.getBalance().currency()).isEqualTo(expected.currency());
        assertThat(wallet.getBalance().amount()).isEqualByComparingTo(expected.amount());
    }

    @Then("the operation should be rejected with message {string}")
    public void theOperationShouldBeRejectedWithMessage(String message) {
        assertThat(caughtException).isNotNull();
        assertThat(caughtException.getMessage()).isEqualTo(message);
    }
}
