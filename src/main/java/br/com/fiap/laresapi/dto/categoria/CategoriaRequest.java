package br.com.fiap.laresapi.dto.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
		@NotBlank @Pattern(regexp = "[A-Za-z0-9_]+") @Size(max = 20) String codigo,
		@NotBlank @Size(max = 60) String nome,
		@Size(max = 255) String descricao,
		@NotNull @Positive Integer prazoPadraoDias) {
}
