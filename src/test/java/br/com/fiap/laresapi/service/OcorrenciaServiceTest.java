package br.com.fiap.laresapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.laresapi.dto.ocorrencia.AtualizarStatusRequest;
import br.com.fiap.laresapi.entity.Ocorrencia;
import br.com.fiap.laresapi.entity.StatusOcorrencia;
import br.com.fiap.laresapi.entity.Usuario;
import br.com.fiap.laresapi.mapper.OcorrenciaMapper;
import br.com.fiap.laresapi.repository.CategoriaRepository;
import br.com.fiap.laresapi.repository.OcorrenciaRepository;
import br.com.fiap.laresapi.repository.UsuarioRepository;

class OcorrenciaServiceTest {

	private OcorrenciaRepository ocorrenciaRepository;
	private UsuarioRepository usuarioRepository;
	private OcorrenciaService ocorrenciaService;

	@BeforeEach
	void setUp() {
		ocorrenciaRepository = mock(OcorrenciaRepository.class);
		usuarioRepository = mock(UsuarioRepository.class);
		ocorrenciaService = new OcorrenciaService(
				ocorrenciaRepository,
				usuarioRepository,
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

	@Test
	void permiteTransicoesDeStatusValidasERegistraHistorico() {
		assertTransicaoValida(StatusOcorrencia.RECEBIDA, StatusOcorrencia.EM_ANDAMENTO);
		assertTransicaoValida(StatusOcorrencia.TRIADA, StatusOcorrencia.EM_ANDAMENTO);
		assertTransicaoValida(StatusOcorrencia.EM_ANDAMENTO, StatusOcorrencia.RESOLVIDA);
	}

	@Test
	void rejeitaTransicaoInvalidaSemAlterarStatusOuHistorico() {
		Ocorrencia ocorrencia = new Ocorrencia();
		ocorrencia.setStatus(StatusOcorrencia.RECEBIDA);
		when(ocorrenciaRepository.findById(7L)).thenReturn(Optional.of(ocorrencia));
		when(usuarioRepository.findById(9L)).thenReturn(Optional.of(new Usuario()));

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> ocorrenciaService.atualizarStatus(
						7L,
						9L,
						new AtualizarStatusRequest(StatusOcorrencia.RESOLVIDA, "Concluído", null)));

		assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
		assertEquals(StatusOcorrencia.RECEBIDA, ocorrencia.getStatus());
		assertEquals(0, ocorrencia.getHistorico().size());
	}

	private void assertTransicaoValida(StatusOcorrencia anterior, StatusOcorrencia novo) {
		Ocorrencia ocorrencia = new Ocorrencia();
		ocorrencia.setStatus(anterior);
		Usuario gestor = new Usuario();
		gestor.setNome("Gestor de teste");
		when(ocorrenciaRepository.findById(7L)).thenReturn(Optional.of(ocorrencia));
		when(usuarioRepository.findById(9L)).thenReturn(Optional.of(gestor));

		ocorrenciaService.atualizarStatus(
				7L,
				9L,
				new AtualizarStatusRequest(novo, "  Atualização de teste  ", null));

		assertEquals(novo, ocorrencia.getStatus());
		assertEquals(1, ocorrencia.getHistorico().size());
		assertEquals(anterior, ocorrencia.getHistorico().get(0).getStatusAnterior());
		assertEquals(novo, ocorrencia.getHistorico().get(0).getStatusNovo());
		assertEquals("Atualização de teste", ocorrencia.getHistorico().get(0).getComentario());
		assertEquals(gestor, ocorrencia.getHistorico().get(0).getUsuario());
	}

	private void assertBadRequest(Runnable chamada) {
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, chamada::run);
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}
}
