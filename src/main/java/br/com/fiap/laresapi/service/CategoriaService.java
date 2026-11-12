package br.com.fiap.laresapi.service;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.laresapi.dto.categoria.CategoriaRequest;
import br.com.fiap.laresapi.dto.categoria.CategoriaResponse;
import br.com.fiap.laresapi.entity.Categoria;
import br.com.fiap.laresapi.mapper.CategoriaMapper;
import br.com.fiap.laresapi.repository.CategoriaRepository;

@Service
public class CategoriaService {

	private final CategoriaRepository categoriaRepository;
	private final CategoriaMapper categoriaMapper;

	public CategoriaService(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
		this.categoriaRepository = categoriaRepository;
		this.categoriaMapper = categoriaMapper;
	}

	@Transactional(readOnly = true)
	public List<CategoriaResponse> listar() {
		return categoriaRepository.findAllByOrderByNomeAsc().stream()
				.map(categoriaMapper::toResponse)
				.toList();
	}

	@Transactional
	public CategoriaResponse criar(CategoriaRequest request) {
		Categoria categoria = new Categoria();
		atualizarCampos(categoria, request);
		return categoriaMapper.toResponse(categoriaRepository.save(categoria));
	}

	@Transactional
	public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
		Categoria categoria = buscar(id);
		atualizarCampos(categoria, request);
		return categoriaMapper.toResponse(categoria);
	}

	@Transactional
	public void excluir(Long id) {
		categoriaRepository.delete(buscar(id));
	}

	private Categoria buscar(Long id) {
		return categoriaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
	}

	private void atualizarCampos(Categoria categoria, CategoriaRequest request) {
		categoria.setCodigo(request.codigo().trim().toUpperCase(Locale.ROOT));
		categoria.setNome(request.nome().trim());
		categoria.setDescricao(request.descricao() == null || request.descricao().isBlank()
				? null
				: request.descricao().trim());
		categoria.setPrazoPadraoDias(request.prazoPadraoDias());
	}
}
