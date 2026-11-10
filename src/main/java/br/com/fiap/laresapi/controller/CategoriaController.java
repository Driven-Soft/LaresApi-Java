package br.com.fiap.laresapi.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.laresapi.dto.categoria.CategoriaResponse;
import br.com.fiap.laresapi.mapper.CategoriaMapper;
import br.com.fiap.laresapi.repository.CategoriaRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/categorias")
@SecurityRequirement(name = "bearerAuth")
public class CategoriaController {

	private final CategoriaRepository categoriaRepository;
	private final CategoriaMapper categoriaMapper;

	public CategoriaController(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
		this.categoriaRepository = categoriaRepository;
		this.categoriaMapper = categoriaMapper;
	}

	@GetMapping
	public List<CategoriaResponse> listar() {
		return categoriaRepository.findAllByOrderByNomeAsc().stream()
				.map(categoriaMapper::toResponse)
				.toList();
	}
}
