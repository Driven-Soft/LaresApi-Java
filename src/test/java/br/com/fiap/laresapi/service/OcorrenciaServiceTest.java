package br.com.fiap.laresapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.laresapi.mapper.OcorrenciaMapper;
import br.com.fiap.laresapi.repository.CategoriaRepository;
import br.com.fiap.laresapi.repository.OcorrenciaRepository;
import br.com.fiap.laresapi.repository.UsuarioRepository;

class OcorrenciaServiceTest {

	private OcorrenciaService ocorrenciaService;

	@BeforeEach
	void setUp() {
		ocorrenciaService = new OcorrenciaService(
				mock(OcorrenciaRepository.class),
				mock(UsuarioRepository.class),
				mock(CategoriaRepository.class),
				mock(OcorrenciaMapper.class),
				mock(ApplicationEventPublisher.class));
	}

	@Test
	void rejeitaPaginacaoInvalidaAntesDeConsultarRepositorio() {
		assertBadRequest(() -> ocorrenciaService.listarMinhas(1L, -1, 10));
		assertBadRequest(() -> ocorrenciaService.listarMinhas(1L, 0, 0));
		assertBadRequest(() -> ocorrenciaService.listarMinhas(1L, 0, -1));
		assertBadRequest(() -> ocorrenciaService.listarMinhas(1L, 0, 101));
		assertBadRequest(() -> ocorrenciaService.buscar(null, null, "dataAbertura", 0, 0));
	}

	private void assertBadRequest(Runnable chamada) {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, chamada::run);
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}
}
