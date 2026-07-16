package com.rubencamero.finflow.domain.entity;

import com.rubencamero.finflow.domain.exception.InvalidWalletException;
import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.UserId;
import com.rubencamero.finflow.domain.valueobject.WalletId;

import java.util.Objects;

public class Wallet {

    private final WalletId id;
    private final UserId ownerId;
    private Money balance;

    public Wallet(UserId ownerId ,Money initialBalance) {
        this.id = WalletId.generate();
        this.ownerId = Objects.requireNonNull(ownerId, "OwnerId cannot be null");
        this.balance = Objects.requireNonNull(initialBalance, "initial balance cannot be null");

        if (initialBalance.isNegative()){
            throw new InvalidWalletException(
                    "Wallet cannot have a negative balance."
            );
        }
    }

    public void deposit(Money amount){
        this.balance = this.balance.add(amount);
    }

    public void withdraw(Money amount){
        Money newBalance = this.balance.subtract(amount);
        if(newBalance.isNegative()){
            throw new InvalidWalletException(
                    "Insufficient balance."
            );
        }
        this.balance = newBalance;
    }

    public WalletId getId() {
        return id;
    }

    public Money getBalance() {
        return balance;
    }

    public UserId getOwnerId(){
        return ownerId;
    }
}
