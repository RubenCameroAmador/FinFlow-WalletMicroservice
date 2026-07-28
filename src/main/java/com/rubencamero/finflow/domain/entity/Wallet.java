package com.rubencamero.finflow.domain.entity;

import com.rubencamero.finflow.domain.event.*;
import com.rubencamero.finflow.domain.exception.InvalidWalletException;
import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletId;
import com.rubencamero.finflow.domain.valueobject.WalletStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Wallet {

    private WalletId id;
    private OwnerId ownerId;
    private Money balance;
    private WalletStatus status;
    private LocalDateTime createdAt;
    private final List<DomainEvent> events;

    public static Wallet reconstitute(WalletId id, OwnerId ownerId, Money balance,
                                      WalletStatus status, LocalDateTime createdAt) {
        Wallet wallet = new Wallet();
        wallet.id = Objects.requireNonNull(id);
        wallet.ownerId = Objects.requireNonNull(ownerId);
        wallet.balance = Objects.requireNonNull(balance);
        wallet.status = Objects.requireNonNull(status);
        wallet.createdAt = Objects.requireNonNull(createdAt);
        return wallet;
    }

    private Wallet() {
        this.events = new ArrayList<>();
    }

    public Wallet(OwnerId ownerId, Money initialBalance, WalletStatus status) {
        this.id = WalletId.generate();
        this.ownerId = Objects.requireNonNull(ownerId, "OwnerId cannot be null");
        this.balance = Objects.requireNonNull(initialBalance, "initial balance cannot be null");
        this.status = Objects.requireNonNull(status);
        this.createdAt = LocalDateTime.now();
        this.events = new ArrayList<>();

        if (initialBalance.isNegative()){
            throw new InvalidWalletException(
                    "Wallet cannot have a negative balance."
            );
        }

        addEvent(new WalletCreated(this.id, this.ownerId, this.balance));
    }

    private void addEvent(DomainEvent event){
        this.events.add(event);
    }

    public List<DomainEvent> pullEvents(){
        List<DomainEvent> snapShot = List.copyOf(this.events);
        this.events.clear();
        return snapShot;
    }

    public void freeze(){
        if ( this.status == WalletStatus.FROZEN)
        {
            throw new InvalidWalletException(
                    "Wallet is already Frozen"
            )   ;
        }

        this.status = WalletStatus.FROZEN;

        addEvent(new WalletFrozen(this.id, this.ownerId));
    }

    public void activate(){
        if ( this.status == WalletStatus.ACTIVE)
        {
            throw new InvalidWalletException(
                    "Wallet is already Activated"
            )   ;
        }
        this.status = WalletStatus.ACTIVE;

        addEvent(new WalletActivated(this.id, this.ownerId));
    }

    public void deposit(Money amount){

        if (this.status == WalletStatus.FROZEN){
            throw new InvalidWalletException(
                    "Wallet is Frozen, you can not deposit money"
            )   ;
        }

        this.balance = this.balance.add(amount);
        addEvent(new MoneyDeposited(this.id, this.ownerId, this.balance, amount));
    }

    public void withdraw(Money amount){
        if ( this.status == WalletStatus.FROZEN)
        {
            throw new InvalidWalletException(
                    "Wallet is frozen, cannot withdraw"
            )   ;
        }

        Money newBalance = this.balance.subtract(amount);

        if(newBalance.isNegative()){
            throw new InvalidWalletException(
                    "Insufficient balance."
            );
        }

        addEvent(new MoneyWithdrawn(this.id, this.ownerId, this.balance, amount));

        this.balance = newBalance;
    }

    public WalletId getId() {
        return id;
    }

    public Money getBalance() {
        return balance;
    }

    public OwnerId getOwnerId(){
        return ownerId;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
