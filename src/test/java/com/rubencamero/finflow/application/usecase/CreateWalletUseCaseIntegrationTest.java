package com.rubencamero.finflow.application.usecase;

import com.rubencamero.finflow.application.command.CreateWalletCommand;
import com.rubencamero.finflow.application.port.DomainEventPublisher;
import com.rubencamero.finflow.application.port.WalletRepository;
import com.rubencamero.finflow.domain.entity.Wallet;
import com.rubencamero.finflow.domain.event.DomainEvent;
import com.rubencamero.finflow.domain.event.WalletCreated;
import com.rubencamero.finflow.domain.valueobject.Money;
import com.rubencamero.finflow.domain.valueobject.OwnerId;
import com.rubencamero.finflow.domain.valueobject.WalletStatus;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Real integration with Postgres (uses the same datasource as application.properties).
 * DomainEventPublisher is replaced with a mock so this test doesn't depend on Kafka.
 * @Transactional rolls back automatically after each test, so it doesn't pollute the dev DB.
 */
@SpringBootTest
@Transactional
class CreateWalletUseCaseIntegrationTest {

    @Autowired
    private CreateWalletUseCase createWalletUseCase;

    @Autowired
    private WalletRepository walletRepository;

    @MockitoBean
    private DomainEventPublisher domainEventPublisher;

    @Test
    void shouldCreateAndPersistAWalletInPostgres() {
        OwnerId ownerId = OwnerId.generate();
        Money initialBalance = new Money(Currency.getInstance("USD"), new BigDecimal("100.00"));
        CreateWalletCommand command = new CreateWalletCommand(ownerId, initialBalance);

        Wallet created = createWalletUseCase.execute(command);

        Optional<Wallet> persisted = walletRepository.findById(created.getId());

        assertThat(persisted).isPresent();
        assertThat(persisted.get().getOwnerId()).isEqualTo(ownerId);
        assertThat(persisted.get().getBalance()).isEqualTo(initialBalance);
        assertThat(persisted.get().getStatus()).isEqualTo(WalletStatus.ACTIVE);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPublishWalletCreatedEvent() {
        OwnerId ownerId = OwnerId.generate();
        Money initialBalance = new Money(Currency.getInstance("USD"), new BigDecimal("50.00"));
        CreateWalletCommand command = new CreateWalletCommand(ownerId, initialBalance);

        createWalletUseCase.execute(command);

        ArgumentCaptor<List<DomainEvent>> eventsCaptor = ArgumentCaptor.forClass(List.class);
        verify(domainEventPublisher).publish(eventsCaptor.capture());

        List<DomainEvent> publishedEvents = eventsCaptor.getValue();
        assertThat(publishedEvents).hasSize(1);
        assertThat(publishedEvents.get(0)).isInstanceOf(WalletCreated.class);
    }
}
