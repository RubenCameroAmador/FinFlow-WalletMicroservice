package com.rubencamero.finflow;

import com.rubencamero.finflow.application.port.DomainEventPublisher;
import com.rubencamero.finflow.application.port.WalletRepository;
import com.rubencamero.finflow.infrastructure.messaging.KafkaDomainEventPublisher;
import com.rubencamero.finflow.infrastructure.persistence.repository.PostgresWalletRepository;
import com.rubencamero.finflow.infrastructure.persistence.repository.WalletJpaRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.rubencamero.finflow.infrastructure.persistence.repository")
public class FinflowApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinflowApiApplication.class, args);
	}

	@Bean
	public WalletRepository walletRepository(WalletJpaRepository jpaRepo) {
		return new PostgresWalletRepository(jpaRepo);
	}

    @Bean
    public DomainEventPublisher domainEventPublisher(KafkaTemplate<Object, Object> kafkaTemplate) {
        return new KafkaDomainEventPublisher(kafkaTemplate);
    }

}
