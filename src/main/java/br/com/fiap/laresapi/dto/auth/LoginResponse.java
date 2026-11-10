package br.com.fiap.laresapi.dto.auth;

import br.com.fiap.laresapi.entity.PapelUsuario;

public record LoginResponse(String token, PapelUsuario papel, String nome, Long usuarioId) {
}
