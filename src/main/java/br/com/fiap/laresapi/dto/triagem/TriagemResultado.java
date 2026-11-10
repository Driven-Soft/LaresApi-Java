package br.com.fiap.laresapi.dto.triagem;

import br.com.fiap.laresapi.entity.PrioridadeOcorrencia;

public record TriagemResultado(
		String categoriaCodigo,
		PrioridadeOcorrencia prioridade,
		int prazoEstimadoDias) {
}
