package com.rubencamero.finflow.bdd;

import com.rubencamero.finflow.application.port.DomainEventPublisher;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Boots the real Spring context (same Postgres as application.properties) for every scenario.
 * DomainEventPublisher is mocked so scenarios don't depend on Kafka being up.
 */
@CucumberContextConfiguration
@SpringBootTest
public class CucumberSpringConfiguration {

    @MockitoBean
    private DomainEventPublisher domainEventPublisher;
}
