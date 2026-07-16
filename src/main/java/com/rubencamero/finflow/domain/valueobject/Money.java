package com.rubencamero.finflow.domain.valueobject;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public class Money {
    private final Currency currency;
    private final BigDecimal amount;

    public Money(Currency currency, BigDecimal amount) {
        this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
        this.currency = Objects.requireNonNull(currency, "Currency cannot be null");

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public Money add(Money other){
        validateSameCurrency(other);

        return new Money(
                this.currency,
                this.amount.add(other.amount)
        );
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);

        return new Money(
                this.currency,
                this.amount.subtract(other.amount)
        );
    }

    public boolean isNegative(){
        return amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isZero(){
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    private void validateSameCurrency(Money other){
        if ( !this.currency.equals(other.currency) ){
            throw new IllegalArgumentException("Currency must match");
        }
    }
}
