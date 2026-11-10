package br.com.fiap.laresapi.dto;

import java.util.Map;

public record ErroResponse(int status, String erro, String mensagem, Map<String, String> campos) {
}
