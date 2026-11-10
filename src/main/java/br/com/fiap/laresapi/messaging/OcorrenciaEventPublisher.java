package br.com.fiap.laresapi.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OcorrenciaEventPublisher {

	private static final Logger LOGGER = LoggerFactory.getLogger(OcorrenciaEventPublisher.class);

	private final RabbitTemplate rabbitTemplate;

	public OcorrenciaEventPublisher(RabbitTemplate rabbitTemplate) {
		this.rabbitTemplate = rabbitTemplate;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void publicarOcorrenciaCriada(OcorrenciaCriadaEvent event) {
		try {
			rabbitTemplate.convertAndSend(
					RabbitMqConfig.EXCHANGE_NAME,
					RabbitMqConfig.ROUTING_KEY,
					event);
		} catch (AmqpException exception) {
			LOGGER.error(
					"Falha ao publicar evento de criação da ocorrência {} no RabbitMQ",
					event.ocorrenciaId(),
					exception);
		}
	}
}
