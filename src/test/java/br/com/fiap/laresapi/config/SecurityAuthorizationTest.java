package br.com.fiap.laresapi.config;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import br.com.fiap.laresapi.controller.OcorrenciaController;
import br.com.fiap.laresapi.service.OcorrenciaService;

@WebMvcTest(OcorrenciaController.class)
@Import({ SecurityConfig.class, SecurityErrorHandler.class, SecurityAuthorizationTest.TestConfig.class })
class SecurityAuthorizationTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void requisicaoSemTokenRetorna401Padronizado() throws Exception {
		mockMvc.perform(get("/ocorrencias"))
				.andExpect(status().isUnauthorized())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	void cidadaoNaoPodeAtualizarStatusDeOcorrencia() throws Exception {
		mockMvc.perform(patch("/ocorrencias/1/status")
				.header("Authorization", "Bearer token-cidadao")
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isForbidden())
				.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.status").value(403));
	}

	@Test
	void expoWebRecebeCorsApenasNaOrigemConfigurada() throws Exception {
		mockMvc.perform(options("/ocorrencias")
				.header("Origin", "http://localhost:8081")
				.header("Access-Control-Request-Method", "GET")
				.header("Access-Control-Request-Headers", "authorization,content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"))
				.andExpect(header().exists("Access-Control-Allow-Headers"));

		mockMvc.perform(options("/ocorrencias")
				.header("Origin", "https://site-invalido.example")
				.header("Access-Control-Request-Method", "GET"))
				.andExpect(status().isForbidden())
				.andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
	}

	@TestConfiguration
	@EnableWebSecurity
	static class TestConfig {

		@Bean
		OcorrenciaService ocorrenciaService() {
			return mock(OcorrenciaService.class);
		}

		@Bean
		JwtDecoder jwtDecoder() {
			return token -> {
				Instant agora = Instant.now();
				return Jwt.withTokenValue(token)
						.header("alg", "none")
						.subject("cidadao.teste@example.com")
						.claim("papel", "CIDADAO")
						.claim("usuarioId", 1L)
						.issuedAt(agora)
						.expiresAt(agora.plusSeconds(3600))
						.build();
			};
		}
	}
}
