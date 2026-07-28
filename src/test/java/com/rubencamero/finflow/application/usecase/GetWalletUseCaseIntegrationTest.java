package com.rubencamero.finflow.application.usecase;

import com.rubencamero.finflow.application.command.GetWalletQuery;
import com.rubencamero.finflow.application.port.WalletRepository;
import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.exception.InvalidWalletException;
import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class GetWalletUseCaseIntegrationTest {

    @Autowired
    private GetWalletUseCase getWalletUseCase;

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void shouldReturnAnExistingWallet() {
        Wallet wallet = new Wallet(OwnerId.generate(), new Money(Currency.getInstance("USD"), new BigDecimal("100.00")));
        walletRepository.save(wallet);

        Wallet found = getWalletUseCase.execute(new GetWalletQuery(wallet.getId()));

        assertThat(found.getId()).isEqualTo(wallet.getId());
        assertThat(found.getOwnerId()).isEqualTo(wallet.getOwnerId());
        assertThat(found.getBalance()).isEqualTo(wallet.getBalance());
    }

    @Test
    void shouldThrowExceptionWhenWalletDoesNotExist() {
        GetWalletQuery query = new GetWalletQuery(WalletId.generate());

        assertThatThrownBy(() -> getWalletUseCase.execute(query))
                .isInstanceOf(InvalidWalletException.class);
    }
}
