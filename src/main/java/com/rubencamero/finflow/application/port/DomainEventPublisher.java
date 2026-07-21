package com.rubencamero.finflow.application.port;

import com.rubencamero.finflow.domain.event.DomainEvent;

import java.util.List;

public interface DomainEventPublisher {
    void publish(List<DomainEvent> events);
}
