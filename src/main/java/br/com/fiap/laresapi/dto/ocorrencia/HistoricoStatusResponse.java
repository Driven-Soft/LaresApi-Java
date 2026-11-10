package br.com.fiap.laresapi.dto.ocorrencia;

import java.time.LocalDateTime;

import br.com.fiap.laresapi.entity.StatusOcorrencia;

public record HistoricoStatusResponse(
		StatusOcorrencia statusAnterior,
		StatusOcorrencia statusNovo,
		String comentario,
		String usuarioNome,
		LocalDateTime dataAlteracao) {
}
