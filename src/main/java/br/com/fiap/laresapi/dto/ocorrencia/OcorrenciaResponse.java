package br.com.fiap.laresapi.dto.ocorrencia;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import br.com.fiap.laresapi.dto.UsuarioResumoResponse;
import br.com.fiap.laresapi.dto.categoria.CategoriaResponse;
import br.com.fiap.laresapi.entity.PrioridadeOcorrencia;
import br.com.fiap.laresapi.entity.StatusOcorrencia;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OcorrenciaResponse(
		Long id,
		String descricao,
		String bairro,
		BigDecimal latitude,
		BigDecimal longitude,
		StatusOcorrencia status,
		PrioridadeOcorrencia prioridade,
		Integer prazoEstimadoDias,
		String resumoIa,
		String mensagemCidadao,
		CategoriaResponse categoria,
		UsuarioResumoResponse usuario,
		LocalDateTime dataAbertura,
		LocalDateTime dataAtualizacao,
		List<HistoricoStatusResponse> historico) {
}
