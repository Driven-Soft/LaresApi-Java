package br.com.fiap.laresapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.PromptUserSpec;
import org.mockito.ArgumentMatchers;

import br.com.fiap.laresapi.dto.ai.ConteudoOcorrenciaIa;

class ConteudoOcorrenciaAiServiceTest {

	private final ChatClient.Builder builder = mock(ChatClient.Builder.class);
	private final ChatClient chatClient = mock(ChatClient.class);
	private final ChatClient.ChatClientRequestSpec request = mock(ChatClient.ChatClientRequestSpec.class);
	private final ChatClient.CallResponseSpec response = mock(ChatClient.CallResponseSpec.class);

	private ConteudoOcorrenciaAiService service;

	@BeforeEach
	void setUp() {
		when(builder.build()).thenReturn(chatClient);
		when(chatClient.prompt()).thenReturn(request);
		when(request.system(any(String.class))).thenReturn(request);
		when(request.user(ArgumentMatchers.<Consumer<PromptUserSpec>>any())).thenReturn(request);
		when(request.call()).thenReturn(response);
		service = new ConteudoOcorrenciaAiService(builder);
	}

	@Test
	void gerarRetornaTextosValidosDaIa() {
		when(response.entity(ConteudoOcorrenciaIa.class))
				.thenReturn(new ConteudoOcorrenciaIa(" Resumo ", " Mensagem ao cidadão "));

		ConteudoOcorrenciaIa resultado = service.gerar(
				"Buraco na via", "Vila Mariana", "BURACO", "ALTA", 12);

		assertEquals("Resumo", resultado.resumoIa());
		assertEquals("Mensagem ao cidadão", resultado.mensagemCidadao());
	}

	@Test
	void gerarRejeitaConteudoIncompletoDaIa() {
		when(response.entity(ConteudoOcorrenciaIa.class))
				.thenReturn(new ConteudoOcorrenciaIa("Resumo", " "));

		assertThrows(
				IllegalStateException.class,
				() -> service.gerar("Buraco na via", "Vila Mariana", "BURACO", "ALTA", 12));
	}
}
