package br.com.fiap.laresapi.dto.triagem;

import java.math.BigDecimal;

public record TriagemApiResponse(
		String categoria,
		String prioridade,
		BigDecimal confianca,
		Integer prazoEstimadoDias,
		String origem) {
}
