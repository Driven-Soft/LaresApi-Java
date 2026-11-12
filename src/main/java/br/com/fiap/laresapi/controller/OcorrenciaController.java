package br.com.fiap.laresapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.laresapi.dto.PaginaResponse;
import br.com.fiap.laresapi.dto.ocorrencia.AtualizarOcorrenciaRequest;
import br.com.fiap.laresapi.dto.ocorrencia.AtualizarStatusRequest;
import br.com.fiap.laresapi.dto.ocorrencia.OcorrenciaRequest;
import br.com.fiap.laresapi.dto.ocorrencia.OcorrenciaResponse;
import br.com.fiap.laresapi.entity.StatusOcorrencia;
import br.com.fiap.laresapi.service.OcorrenciaService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/ocorrencias")
@SecurityRequirement(name = "bearerAuth")
public class OcorrenciaController {

	private final OcorrenciaService ocorrenciaService;

	public OcorrenciaController(OcorrenciaService ocorrenciaService) {
		this.ocorrenciaService = ocorrenciaService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OcorrenciaResponse criar(
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody OcorrenciaRequest request) {
		return ocorrenciaService.criar(usuarioId(jwt), request);
	}

	@GetMapping("/minhas")
	public PaginaResponse<OcorrenciaResponse> listarMinhas(
			@AuthenticationPrincipal Jwt jwt,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ocorrenciaService.listarMinhas(usuarioId(jwt), page, size);
	}

	@GetMapping
	public PaginaResponse<OcorrenciaResponse> buscar(
			@RequestParam(required = false) String bairro,
			@RequestParam(required = false) StatusOcorrencia status,
			@RequestParam(defaultValue = "dataAbertura") String sort,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ocorrenciaService.buscar(bairro, status, sort, page, size);
	}

	@GetMapping("/{id}")
	public OcorrenciaResponse buscarPorId(@PathVariable Long id) {
		return ocorrenciaService.buscarPorId(id);
	}

	@PutMapping("/{id}")
	public OcorrenciaResponse atualizar(
			@PathVariable Long id,
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody AtualizarOcorrenciaRequest request) {
		return ocorrenciaService.atualizar(id, usuarioId(jwt), request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void excluir(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
		ocorrenciaService.excluir(id, usuarioId(jwt));
	}

	@PatchMapping("/{id}/status")
	public OcorrenciaResponse atualizarStatus(
			@PathVariable Long id,
			@AuthenticationPrincipal Jwt jwt,
			@Valid @RequestBody AtualizarStatusRequest request) {
		return ocorrenciaService.atualizarStatus(id, usuarioId(jwt), request);
	}

	private Long usuarioId(Jwt jwt) {
		Object claim = jwt.getClaims().get("usuarioId");
		if (claim instanceof Number usuarioId) {
			return usuarioId.longValue();
		}
		throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token sem identificador de usuário válido");
	}
}
