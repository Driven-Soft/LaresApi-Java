package br.com.fiap.laresapi.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import br.com.fiap.laresapi.controller.CategoriaController;
import br.com.fiap.laresapi.controller.OcorrenciaController;
import br.com.fiap.laresapi.dto.categoria.CategoriaRequest;
import br.com.fiap.laresapi.dto.categoria.CategoriaResponse;
import br.com.fiap.laresapi.service.CategoriaService;
import br.com.fiap.laresapi.service.OcorrenciaService;

@WebMvcTest({ OcorrenciaController.class, CategoriaController.class })
@Import({ SecurityConfig.class, SecurityErrorHandler.class, SecurityAuthorizationTest.TestConfig.class })
class SecurityAuthorizationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoriaService categoriaService;

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

	@Test
	void cidadaoNaoPodeModificarCategorias() throws Exception {
		String corpo = """
				{
				  "codigo": "NOVA_CATEGORIA",
				  "nome": "Nova categoria",
				  "descricao": "Descrição de teste",
				  "prazoPadraoDias": 14
				}
				""";

		mockMvc.perform(post("/categorias")
				.header("Authorization", "Bearer token-cidadao")
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo))
				.andExpect(status().isForbidden());

		mockMvc.perform(put("/categorias/1")
				.header("Authorization", "Bearer token-cidadao")
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpo))
				.andExpect(status().isForbidden());

		mockMvc.perform(delete("/categorias/1")
				.header("Authorization", "Bearer token-cidadao"))
				.andExpect(status().isForbidden());
	}

	@Test
	void gestorPodeCriarCategoria() throws Exception {
		when(categoriaService.criar(any(CategoriaRequest.class)))
				.thenReturn(new CategoriaResponse(
						1L,
						"NOVA_CATEGORIA",
						"Nova categoria",
						"Descrição de teste",
						14));

		mockMvc.perform(post("/categorias")
				.header("Authorization", "Bearer token-gestor")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "codigo": "NOVA_CATEGORIA",
						  "nome": "Nova categoria",
						  "descricao": "Descrição de teste",
						  "prazoPadraoDias": 14
						}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.codigo").value("NOVA_CATEGORIA"))
				.andExpect(jsonPath("$.prazoPadraoDias").value(14));
	}

	@TestConfiguration
	@EnableWebSecurity
	static class TestConfig {

		@Bean
		OcorrenciaService ocorrenciaService() {
			return mock(OcorrenciaService.class);
		}

		@Bean
		CategoriaService categoriaService() {
			return mock(CategoriaService.class);
		}

		@Bean
		JwtDecoder jwtDecoder() {
			return token -> {
				Instant agora = Instant.now();
				String papel = "token-gestor".equals(token) ? "GESTOR" : "CIDADAO";
				return Jwt.withTokenValue(token)
						.header("alg", "none")
						.subject("cidadao.teste@example.com")
						.claim("papel", papel)
						.claim("usuarioId", 1L)
						.issuedAt(agora)
						.expiresAt(agora.plusSeconds(3600))
						.build();
			};
		}
	}
}
