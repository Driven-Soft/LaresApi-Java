package br.com.fiap.laresapi.dto.ocorrencia;

import br.com.fiap.laresapi.entity.StatusOcorrencia;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AtualizarStatusRequest(
		@NotNull StatusOcorrencia status,
		@Size(max = 255) String comentario,
		@Positive Long categoriaId) {
}
