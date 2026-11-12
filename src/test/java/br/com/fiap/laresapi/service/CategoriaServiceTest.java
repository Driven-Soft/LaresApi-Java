package br.com.fiap.laresapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.laresapi.dto.categoria.CategoriaRequest;
import br.com.fiap.laresapi.dto.categoria.CategoriaResponse;
import br.com.fiap.laresapi.entity.Categoria;
import br.com.fiap.laresapi.mapper.CategoriaMapper;
import br.com.fiap.laresapi.repository.CategoriaRepository;

class CategoriaServiceTest {

	private CategoriaRepository categoriaRepository;
	private CategoriaMapper categoriaMapper;
	private CategoriaService categoriaService;

	@BeforeEach
	void setUp() {
		categoriaRepository = mock(CategoriaRepository.class);
		categoriaMapper = mock(CategoriaMapper.class);
		categoriaService = new CategoriaService(categoriaRepository, categoriaMapper);
	}

	@Test
	void criarNormalizaCodigoECamposTexto() {
		when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
			Categoria categoria = invocation.getArgument(0);
			return categoria;
		});
		when(categoriaMapper.toResponse(any(Categoria.class))).thenAnswer(invocation -> {
			Categoria categoria = invocation.getArgument(0);
			return new CategoriaResponse(
					1L,
					categoria.getCodigo(),
					categoria.getNome(),
					categoria.getDescricao(),
					categoria.getPrazoPadraoDias());
		});

		CategoriaResponse response = categoriaService.criar(
				new CategoriaRequest("agua_potavel", " Água potável ", "  ", 14));

		assertEquals("AGUA_POTAVEL", response.codigo());
		assertEquals("Água potável", response.nome());
		assertNull(response.descricao());
		assertEquals(14, response.prazoPadraoDias());
		verify(categoriaRepository).save(any(Categoria.class));
	}

	@Test
	void atualizarCategoriaInexistenteRetorna404() {
		when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(
				ResponseStatusException.class,
				() -> categoriaService.atualizar(
						99L,
						new CategoriaRequest("NOVA", "Nova", null, 14)));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	@Test
	void atualizarAlteraCamposDaCategoriaExistente() {
		Categoria categoria = new Categoria();
		categoria.setCodigo("AGUA");
		categoria.setNome("Água");
		categoria.setPrazoPadraoDias(10);
		when(categoriaRepository.findById(7L)).thenReturn(Optional.of(categoria));
		when(categoriaMapper.toResponse(categoria)).thenReturn(
				new CategoriaResponse(7L, "AGUA_POTAVEL", "Água potável", "Vazamentos", 14));

		CategoriaResponse response = categoriaService.atualizar(
				7L,
				new CategoriaRequest("agua_potavel", "Água potável", "Vazamentos", 14));

		assertEquals("AGUA_POTAVEL", categoria.getCodigo());
		assertEquals("Água potável", categoria.getNome());
		assertEquals(14, categoria.getPrazoPadraoDias());
		assertEquals("AGUA_POTAVEL", response.codigo());
	}

	@Test
	void excluirRemoveCategoriaExistente() {
		Categoria categoria = new Categoria();
		when(categoriaRepository.findById(7L)).thenReturn(Optional.of(categoria));

		categoriaService.excluir(7L);

		verify(categoriaRepository).delete(categoria);
	}
}
