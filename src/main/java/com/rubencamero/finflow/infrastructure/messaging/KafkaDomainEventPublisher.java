package com.rubencamero.finflow.infrastructure.messaging;

import com.rubencamero.finflow.application.port.DomainEventPublisher;
import com.rubencamero.finflow.domain.event.DomainEvent;
import com.rubencamero.finflow.infrastructure.messaging.event.WalletIntegrationEvent;
import com.rubencamero.finflow.infrastructure.messaging.mapper.WalletEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

public class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);
    private static final String TOPIC = "wallet-events";

    private final KafkaTemplate<Object, Object> kafkaTemplate;

    public KafkaDomainEventPublisher(KafkaTemplate<Object, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            WalletIntegrationEvent integrationEvent = WalletEventMapper.toIntegrationEvent(event);
            String key = integrationEvent.walletId().toString();

            kafkaTemplate.send(TOPIC, key, integrationEvent)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Error publicando {} para wallet {}", integrationEvent.eventType(), key, ex);
                        } else {
                            log.info("Evento {} publicado en partición {}", integrationEvent.eventType(),
                                    result.getRecordMetadata().partition());
                        }
                    });
        }
    }
}