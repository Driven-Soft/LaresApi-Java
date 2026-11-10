package br.com.fiap.laresapi.dto.auth;

import br.com.fiap.laresapi.entity.PapelUsuario;

public record RegistroResponse(Long id, String nome, String email, PapelUsuario papel) {
}
