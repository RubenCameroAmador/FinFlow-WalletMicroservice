package com.rubencamero.finflow.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {
    UUID eventId();
    LocalDateTime occurredOn();
}
