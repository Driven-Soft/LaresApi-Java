package br.com.fiap.laresapi.messaging;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.read.ListAppender;
import br.com.fiap.laresapi.dto.triagem.TriagemRequest;
import br.com.fiap.laresapi.dto.triagem.TriagemResultado;
import br.com.fiap.laresapi.entity.PrioridadeOcorrencia;
import br.com.fiap.laresapi.integration.TriagemClient;
import br.com.fiap.laresapi.service.ConteudoOcorrenciaAiService;
import br.com.fiap.laresapi.service.TriagemService;

@ExtendWith(MockitoExtension.class)
class OcorrenciaTriagemConsumerTest {

	private static final Long OCORRENCIA_ID = 42L;
	private static final TriagemRequest REQUEST = new TriagemRequest("Buraco na rua", "Centro");
	private static final TriagemResultado RESULTADO =
			new TriagemResultado("BURACO", PrioridadeOcorrencia.ALTA, 10);

	@Mock
	private TriagemService triagemService;

	@Mock
	private TriagemClient triagemClient;

	@Mock
	private ConteudoOcorrenciaAiService conteudoAiService;

	@InjectMocks
	private OcorrenciaTriagemConsumer consumer;

	@Test
	void falhaDaIaGravaTriagemSemTextosERegistraDiagnosticoSemPayload() {
		String dadoSensivel = "conteudo-sensivel";
		RestClientResponseException erroHttp = new RestClientResponseException(
				dadoSensivel,
				HttpStatus.TOO_MANY_REQUESTS,
				"Too Many Requests",
				HttpHeaders.EMPTY,
				dadoSensivel.getBytes(StandardCharsets.UTF_8),
				StandardCharsets.UTF_8);
		when(triagemService.obterDados(OCORRENCIA_ID)).thenReturn(Optional.of(REQUEST));
		when(triagemClient.classificar(REQUEST)).thenReturn(RESULTADO);
		when(conteudoAiService.gerar(
				REQUEST.descricao(),
				REQUEST.bairro(),
				RESULTADO.categoriaCodigo(),
				RESULTADO.prioridade().name(),
				RESULTADO.prazoEstimadoDias()))
				.thenThrow(new IllegalStateException(dadoSensivel, erroHttp));

		Logger logger = (Logger) LoggerFactory.getLogger(OcorrenciaTriagemConsumer.class);
		ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);

		try {
			consumer.consumir(new OcorrenciaCriadaEvent(OCORRENCIA_ID));
		} finally {
			logger.detachAppender(appender);
			appender.stop();
		}

		verify(triagemService).aplicarResultado(OCORRENCIA_ID, RESULTADO, null);
		String log = appender.list.get(0).getFormattedMessage();
		assertTrue(log.contains("erro=java.lang.IllegalStateException"));
		assertTrue(log.contains("causa=org.springframework.web.client.RestClientResponseException"));
		assertTrue(log.contains("httpStatus=429"));
		assertFalse(log.contains(dadoSensivel));
		assertNull(appender.list.get(0).getThrowableProxy());
	}
}
