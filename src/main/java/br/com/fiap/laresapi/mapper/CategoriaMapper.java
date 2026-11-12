package br.com.fiap.laresapi.mapper;

import org.springframework.stereotype.Component;

import br.com.fiap.laresapi.dto.categoria.CategoriaResponse;
import br.com.fiap.laresapi.entity.Categoria;

@Component
public class CategoriaMapper {

	public CategoriaResponse toResponse(Categoria categoria) {
		return new CategoriaResponse(
				categoria.getId(),
				categoria.getCodigo(),
				categoria.getNome(),
				categoria.getDescricao(),
				categoria.getPrazoPadraoDias());
	}
}
