package br.com.fiap.laresapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.fiap.laresapi.dto.triagem.TriagemResultado;
import br.com.fiap.laresapi.entity.Categoria;
import br.com.fiap.laresapi.entity.Ocorrencia;
import br.com.fiap.laresapi.entity.PrioridadeOcorrencia;
import br.com.fiap.laresapi.entity.StatusOcorrencia;
import br.com.fiap.laresapi.repository.CategoriaRepository;
import br.com.fiap.laresapi.repository.OcorrenciaRepository;

@ExtendWith(MockitoExtension.class)
class TriagemServiceTest {

	@Mock
	private OcorrenciaRepository ocorrenciaRepository;

	@Mock
	private CategoriaRepository categoriaRepository;

	@InjectMocks
	private TriagemService triagemService;

	private Ocorrencia ocorrencia;

	@BeforeEach
	void setUp() {
		ocorrencia = new Ocorrencia();
		ocorrencia.setStatus(StatusOcorrencia.RECEBIDA);
	}

	@Test
	void aplicarResultadoAtualizaOcorrenciaEGravaHistoricoDaIa() {
		Categoria categoria = new Categoria();
		categoria.setCodigo("BURACO");
		when(ocorrenciaRepository.findById(10L)).thenReturn(Optional.of(ocorrencia));
		when(categoriaRepository.findByCodigo("BURACO")).thenReturn(Optional.of(categoria));

		triagemService.aplicarResultado(
				10L,
				new TriagemResultado("BURACO", PrioridadeOcorrencia.ALTA, 12));

		assertEquals(StatusOcorrencia.TRIADA, ocorrencia.getStatus());
		assertEquals(PrioridadeOcorrencia.ALTA, ocorrencia.getPrioridade());
		assertEquals(12, ocorrencia.getPrazoEstimadoDias());
		assertEquals(categoria, ocorrencia.getCategoria());
		assertEquals(1, ocorrencia.getHistorico().size());
		assertEquals(StatusOcorrencia.RECEBIDA, ocorrencia.getHistorico().get(0).getStatusAnterior());
		assertEquals(StatusOcorrencia.TRIADA, ocorrencia.getHistorico().get(0).getStatusNovo());
		assertNull(ocorrencia.getHistorico().get(0).getUsuario());
		verify(ocorrenciaRepository).save(ocorrencia);
	}

	@Test
	void categoriaDesconhecidaNaoAlteraOcorrencia() {
		when(ocorrenciaRepository.findById(10L)).thenReturn(Optional.of(ocorrencia));
		when(categoriaRepository.findByCodigo("DESCONHECIDA")).thenReturn(Optional.empty());

		assertThrows(
				IllegalArgumentException.class,
				() -> triagemService.aplicarResultado(
						10L,
						new TriagemResultado("DESCONHECIDA", PrioridadeOcorrencia.ALTA, 12)));

		assertEquals(StatusOcorrencia.RECEBIDA, ocorrencia.getStatus());
		assertEquals(0, ocorrencia.getHistorico().size());
	}
}
