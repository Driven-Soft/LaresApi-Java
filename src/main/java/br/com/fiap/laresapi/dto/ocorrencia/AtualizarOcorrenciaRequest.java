package br.com.fiap.laresapi.dto.ocorrencia;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AtualizarOcorrenciaRequest(
		@NotBlank @Size(max = 500) String descricao,
		@NotBlank @Size(max = 80) String bairro,
		@DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
		@DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude) {
}
