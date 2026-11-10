package br.com.fiap.laresapi.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

	public static final String QUEUE_NAME = "ocorrencia.criada";
	public static final String EXCHANGE_NAME = "lares.eventos";
	public static final String ROUTING_KEY = "ocorrencia.criada";

	@Bean
	Queue ocorrenciaCriadaQueue() {
		return QueueBuilder.durable(QUEUE_NAME).build();
	}

	@Bean
	DirectExchange laresExchange() {
		return new DirectExchange(EXCHANGE_NAME, true, false);
	}

	@Bean
	Binding ocorrenciaCriadaBinding(Queue ocorrenciaCriadaQueue, DirectExchange laresExchange) {
		return BindingBuilder.bind(ocorrenciaCriadaQueue).to(laresExchange).with(ROUTING_KEY);
	}

	@Bean
	JacksonJsonMessageConverter jacksonJsonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}
}
