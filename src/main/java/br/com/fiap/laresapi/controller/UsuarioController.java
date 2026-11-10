package br.com.fiap.laresapi.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.fiap.laresapi.dto.auth.UsuarioMeResponse;
import br.com.fiap.laresapi.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

	private final UsuarioRepository usuarioRepository;

	public UsuarioController(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@GetMapping("/me")
	public UsuarioMeResponse me(@AuthenticationPrincipal Jwt jwt) {
		Object claim = jwt.getClaims().get("usuarioId");
		if (!(claim instanceof Number usuarioId)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token sem identificador de usuário válido");
		}
		var usuario = usuarioRepository.findById(usuarioId.longValue())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
		return new UsuarioMeResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPapel());
	}
}
