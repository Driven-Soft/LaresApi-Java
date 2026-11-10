package br.com.fiap.laresapi.integration;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.fiap.laresapi.dto.triagem.TriagemApiResponse;
import br.com.fiap.laresapi.dto.triagem.TriagemRequest;
import br.com.fiap.laresapi.dto.triagem.TriagemResultado;
import br.com.fiap.laresapi.entity.PrioridadeOcorrencia;

@Component
public class TriagemClient {

	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(90);

	private final RestClient restClient;
	private final String apiKey;

	public TriagemClient(
			RestClient.Builder builder,
			@Value("${app.triagem.base-url}") String baseUrl,
			@Value("${app.triagem.api-key:}") String apiKey) {
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(CONNECT_TIMEOUT)
				.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(READ_TIMEOUT);
		this.restClient = builder
				.baseUrl(baseUrl)
				.requestFactory(requestFactory)
				.build();
		this.apiKey = apiKey;
	}

	public TriagemResultado classificar(TriagemRequest request) {
		TriagemApiResponse response = restClient.post()
				.uri("/triagem")
				.contentType(MediaType.APPLICATION_JSON)
				.headers(headers -> {
					if (apiKey != null && !apiKey.isBlank()) {
						headers.set("X-API-Key", apiKey);
					}
				})
				.body(request)
				.retrieve()
				.body(TriagemApiResponse.class);

		if (response == null
				|| response.categoria() == null
				|| response.categoria().isBlank()
				|| response.prioridade() == null
				|| response.prazoEstimadoDias() == null
				|| response.prazoEstimadoDias() <= 0) {
			throw new IllegalStateException("Resposta inválida recebida do serviço de triagem");
		}

		PrioridadeOcorrencia prioridade;
		try {
			prioridade = PrioridadeOcorrencia.valueOf(response.prioridade().trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException exception) {
			throw new IllegalArgumentException("Prioridade inválida recebida do serviço de triagem", exception);
		}

		return new TriagemResultado(
				response.categoria().trim().toUpperCase(Locale.ROOT),
				prioridade,
				response.prazoEstimadoDias());
	}
}
