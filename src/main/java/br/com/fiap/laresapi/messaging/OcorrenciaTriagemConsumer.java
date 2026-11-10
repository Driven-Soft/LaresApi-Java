package br.com.fiap.laresapi.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.laresapi.dto.triagem.TriagemRequest;
import br.com.fiap.laresapi.dto.triagem.TriagemResultado;
import br.com.fiap.laresapi.integration.TriagemClient;
import br.com.fiap.laresapi.service.TriagemService;

@Component
public class OcorrenciaTriagemConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(OcorrenciaTriagemConsumer.class);

	private final TriagemService triagemService;
	private final TriagemClient triagemClient;

	public OcorrenciaTriagemConsumer(TriagemService triagemService, TriagemClient triagemClient) {
		this.triagemService = triagemService;
		this.triagemClient = triagemClient;
	}

	@RabbitListener(queues = RabbitMqConfig.QUEUE_NAME)
	public void consumir(OcorrenciaCriadaEvent event) {
		try {
			TriagemRequest request = triagemService.obterDados(event.ocorrenciaId()).orElse(null);
			if (request == null) {
				LOGGER.info(
						"A ocorrência {} não existe ou já foi processada; o evento será ignorado",
						event.ocorrenciaId());
				return;
			}

			TriagemResultado resultado = triagemClient.classificar(request);
			triagemService.aplicarResultado(event.ocorrenciaId(), resultado);
		} catch (RestClientException | IllegalArgumentException | IllegalStateException | ResponseStatusException exception) {
			LOGGER.error(
					"Falha ao triar a ocorrência {}; ela permanece com status RECEBIDA",
					event.ocorrenciaId(),
					exception);
		}
	}
}
