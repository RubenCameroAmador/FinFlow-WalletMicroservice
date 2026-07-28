package com.rubencamero.finflow.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    private final Currency usd = Currency.getInstance("USD");
    private final Currency eur = Currency.getInstance("EUR");

    @Test
    void constructorShouldRejectNegativeAmount() {
        assertThatThrownBy(() -> new Money(usd, new BigDecimal("-1.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructorShouldRejectNullAmount() {
        assertThatThrownBy(() -> new Money(usd, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructorShouldRejectNullCurrency() {
        assertThatThrownBy(() -> new Money(null, BigDecimal.TEN))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void addShouldSumAmountsOfSameCurrency() {
        Money a = new Money(usd, new BigDecimal("10.00"));
        Money b = new Money(usd, new BigDecimal("5.00"));

        Money result = a.add(b);

        assertThat(result.amount()).isEqualByComparingTo("15.00");
        assertThat(result.currency()).isEqualTo(usd);
    }

    @Test
    void addShouldThrowExceptionWhenCurrenciesDiffer() {
        Money a = new Money(usd, new BigDecimal("10.00"));
        Money b = new Money(eur, new BigDecimal("5.00"));

        assertThatThrownBy(() -> a.add(b)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void subtractShouldSubtractAmountsOfSameCurrency() {
        Money a = new Money(usd, new BigDecimal("10.00"));
        Money b = new Money(usd, new BigDecimal("4.00"));

        Money result = a.subtract(b);

        assertThat(result.amount()).isEqualByComparingTo("6.00");
    }

    @Test
    void subtractShouldThrowExceptionWhenCurrenciesDiffer() {
        Money a = new Money(usd, new BigDecimal("10.00"));
        Money b = new Money(eur, new BigDecimal("4.00"));

        assertThatThrownBy(() -> a.subtract(b)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void subtractShouldThrowIllegalArgumentExceptionWhenResultWouldBeNegative() {
        // Money has no concept of "insufficient funds" - it just refuses to hold a negative
        // value at all. Callers (see Wallet.withdraw) must check with isLessThan(...) beforehand.
        Money a = new Money(usd, new BigDecimal("5.00"));
        Money b = new Money(usd, new BigDecimal("10.00"));

        assertThatThrownBy(() -> a.subtract(b)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void isZeroShouldReturnTrueForZeroAmount() {
        Money zero = new Money(usd, BigDecimal.ZERO);

        assertThat(zero.isZero()).isTrue();
    }

    @Test
    void isZeroShouldReturnFalseForNonZeroAmount() {
        Money money = new Money(usd, new BigDecimal("1.00"));

        assertThat(money.isZero()).isFalse();
    }

    @Test
    void isLessThanShouldReturnTrueWhenAmountIsSmaller() {
        Money five = new Money(usd, new BigDecimal("5.00"));
        Money ten = new Money(usd, new BigDecimal("10.00"));

        assertThat(five.isLessThan(ten)).isTrue();
        assertThat(ten.isLessThan(five)).isFalse();
    }

    @Test
    void isLessThanShouldReturnFalseWhenAmountsAreEqual() {
        Money a = new Money(usd, new BigDecimal("5.00"));
        Money b = new Money(usd, new BigDecimal("5.00"));

        assertThat(a.isLessThan(b)).isFalse();
    }

    @Test
    void isLessThanShouldThrowExceptionWhenCurrenciesDiffer() {
        Money a = new Money(usd, new BigDecimal("5.00"));
        Money b = new Money(eur, new BigDecimal("5.00"));

        assertThatThrownBy(() -> a.isLessThan(b)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void equalsAndHashCodeShouldBeBasedOnCurrencyAndAmount() {
        Money a = new Money(usd, new BigDecimal("10.00"));
        Money b = new Money(usd, new BigDecimal("10.00"));

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void equalsShouldReturnFalseForDifferentAmount() {
        Money a = new Money(usd, new BigDecimal("10.00"));
        Money b = new Money(usd, new BigDecimal("20.00"));

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void equalsShouldReturnFalseForDifferentCurrency() {
        Money a = new Money(usd, new BigDecimal("10.00"));
        Money b = new Money(eur, new BigDecimal("10.00"));

        assertThat(a).isNotEqualTo(b);
    }
}
