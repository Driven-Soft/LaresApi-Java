package br.com.fiap.laresapi.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.laresapi.dto.auth.LoginRequest;
import br.com.fiap.laresapi.dto.auth.LoginResponse;
import br.com.fiap.laresapi.dto.auth.RegistroRequest;
import br.com.fiap.laresapi.dto.auth.RegistroResponse;
import br.com.fiap.laresapi.entity.PapelUsuario;
import br.com.fiap.laresapi.entity.Usuario;
import br.com.fiap.laresapi.repository.UsuarioRepository;

@Service
public class AutenticacaoService {

	private static final Duration TOKEN_VALIDITY = Duration.ofHours(24);

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtEncoder jwtEncoder;

	public AutenticacaoService(
			UsuarioRepository usuarioRepository,
			PasswordEncoder passwordEncoder,
			JwtEncoder jwtEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtEncoder = jwtEncoder;
	}

	@Transactional
	public RegistroResponse registrar(RegistroRequest request) {
		String email = normalizarEmail(request.email());
		if (usuarioRepository.existsByEmailIgnoreCase(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado");
		}

		Usuario usuario = new Usuario();
		usuario.setNome(request.nome().trim());
		usuario.setEmail(email);
		usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
		usuario.setPapel(PapelUsuario.CIDADAO);
		usuario.setDataCadastro(LocalDateTime.now());

		Usuario salvo = usuarioRepository.save(usuario);
		return new RegistroResponse(salvo.getId(), salvo.getNome(), salvo.getEmail(), salvo.getPapel());
	}

	@Transactional(readOnly = true)
	public LoginResponse autenticar(LoginRequest request) {
		String email = normalizarEmail(request.email());
		Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
				.filter(candidato -> passwordEncoder.matches(request.senha(), candidato.getSenhaHash()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "E-mail ou senha inválidos"));

		Instant agora = Instant.now();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("lares-api")
				.subject(usuario.getEmail())
				.issuedAt(agora)
				.expiresAt(agora.plus(TOKEN_VALIDITY))
				.claim("usuarioId", usuario.getId())
				.claim("papel", usuario.getPapel().name())
				.build();

		String token = jwtEncoder.encode(JwtEncoderParameters.from(
				JwsHeader.with(MacAlgorithm.HS256).build(),
				claims)).getTokenValue();

		return new LoginResponse(token, usuario.getPapel(), usuario.getNome(), usuario.getId());
	}

	private String normalizarEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}
}
