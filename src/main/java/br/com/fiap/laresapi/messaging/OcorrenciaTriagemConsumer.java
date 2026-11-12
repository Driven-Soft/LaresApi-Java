package br.com.fiap.laresapi.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.laresapi.dto.ai.ConteudoOcorrenciaIa;
import br.com.fiap.laresapi.dto.triagem.TriagemRequest;
import br.com.fiap.laresapi.dto.triagem.TriagemResultado;
import br.com.fiap.laresapi.integration.TriagemClient;
import br.com.fiap.laresapi.service.ConteudoOcorrenciaAiService;
import br.com.fiap.laresapi.service.TriagemService;

@Component
public class OcorrenciaTriagemConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(OcorrenciaTriagemConsumer.class);

	private final TriagemService triagemService;
	private final TriagemClient triagemClient;
	private final ConteudoOcorrenciaAiService conteudoAiService;

	public OcorrenciaTriagemConsumer(
			TriagemService triagemService,
			TriagemClient triagemClient,
			ConteudoOcorrenciaAiService conteudoAiService) {
		this.triagemService = triagemService;
		this.triagemClient = triagemClient;
		this.conteudoAiService = conteudoAiService;
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
			ConteudoOcorrenciaIa conteudo = gerarConteudo(event.ocorrenciaId(), request, resultado);
			triagemService.aplicarResultado(event.ocorrenciaId(), resultado, conteudo);
		} catch (RestClientException | IllegalArgumentException | IllegalStateException | ResponseStatusException exception) {
			LOGGER.error(
					"Falha ao triar a ocorrência {}; ela permanece com status RECEBIDA",
					event.ocorrenciaId(),
					exception);
		}
	}

	private ConteudoOcorrenciaIa gerarConteudo(Long ocorrenciaId, TriagemRequest request, TriagemResultado resultado) {
		try {
			return conteudoAiService.gerar(
					request.descricao(),
					request.bairro(),
					resultado.categoriaCodigo(),
					resultado.prioridade().name(),
					resultado.prazoEstimadoDias());
		} catch (RuntimeException exception) {
			LOGGER.warn(
					"Falha ao gerar os textos de IA da ocorrência {}; a triagem será gravada sem eles "
							+ "(erro={}, causa={}, httpStatus={})",
					ocorrenciaId,
					exception.getClass().getName(),
					tipoDaCausa(exception),
					statusHttp(exception));
			return null;
		}
	}

	private static String tipoDaCausa(RuntimeException exception) {
		Throwable cause = exception.getCause();
		return cause == null ? "indisponivel" : cause.getClass().getName();
	}

	private static String statusHttp(RuntimeException exception) {
		Throwable cause = exception;
		while (cause != null) {
			if (cause instanceof RestClientResponseException responseException) {
				return String.valueOf(responseException.getStatusCode().value());
			}
			if (cause.getCause() == cause) {
				break;
			}
			cause = cause.getCause();
		}
		return "indisponivel";
	}
}