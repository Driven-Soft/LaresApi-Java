package br.com.fiap.laresapi.dto.categoria;

public record CategoriaResponse(Long id, String codigo, String nome, String descricao, Integer prazoPadraoDias) {
}
