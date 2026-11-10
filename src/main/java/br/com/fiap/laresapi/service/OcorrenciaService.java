package br.com.fiap.laresapi.service;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.laresapi.dto.PaginaResponse;
import br.com.fiap.laresapi.dto.ocorrencia.AtualizarOcorrenciaRequest;
import br.com.fiap.laresapi.dto.ocorrencia.OcorrenciaRequest;
import br.com.fiap.laresapi.dto.ocorrencia.OcorrenciaResponse;
import br.com.fiap.laresapi.entity.HistoricoStatus;
import br.com.fiap.laresapi.entity.Ocorrencia;
import br.com.fiap.laresapi.entity.StatusOcorrencia;
import br.com.fiap.laresapi.entity.Usuario;
import br.com.fiap.laresapi.mapper.OcorrenciaMapper;
import br.com.fiap.laresapi.repository.OcorrenciaRepository;
import br.com.fiap.laresapi.repository.UsuarioRepository;

@Service
public class OcorrenciaService {

	private static final int MAX_PAGE_SIZE = 100;

	private final OcorrenciaRepository ocorrenciaRepository;
	private final UsuarioRepository usuarioRepository;
	private final OcorrenciaMapper ocorrenciaMapper;

	public OcorrenciaService(
			OcorrenciaRepository ocorrenciaRepository,
			UsuarioRepository usuarioRepository,
			OcorrenciaMapper ocorrenciaMapper) {
		this.ocorrenciaRepository = ocorrenciaRepository;
		this.usuarioRepository = usuarioRepository;
		this.ocorrenciaMapper = ocorrenciaMapper;
	}

	@Transactional
	public OcorrenciaResponse criar(Long usuarioId, OcorrenciaRequest request) {
		Usuario usuario = buscarUsuario(usuarioId);
		LocalDateTime agora = LocalDateTime.now();

		Ocorrencia ocorrencia = new Ocorrencia();
		ocorrencia.setUsuario(usuario);
		ocorrencia.setDescricao(request.descricao().trim());
		ocorrencia.setBairro(request.bairro().trim());
		ocorrencia.setLatitude(request.latitude());
		ocorrencia.setLongitude(request.longitude());
		ocorrencia.setStatus(StatusOcorrencia.RECEBIDA);
		ocorrencia.setDataAbertura(agora);
		ocorrencia.setDataAtualizacao(agora);
		ocorrencia.adicionarHistorico(criarHistorico(ocorrencia, usuario, null, StatusOcorrencia.RECEBIDA, null, agora));

		Ocorrencia salva = ocorrenciaRepository.save(ocorrencia);
		return ocorrenciaMapper.toResponse(salva, false);
	}

	@Transactional(readOnly = true)
	public PaginaResponse<OcorrenciaResponse> listarMinhas(Long usuarioId, PageRequest pageRequest) {
		validarPaginacao(pageRequest);
		Page<Ocorrencia> pagina = ocorrenciaRepository.findByUsuarioId(usuarioId, pageRequest);
		return paraPagina(pagina);
	}

	@Transactional(readOnly = true)
	public PaginaResponse<OcorrenciaResponse> buscar(
			String bairro,
			StatusOcorrencia status,
			String sort,
			PageRequest pageRequest) {
		validarPaginacao(pageRequest);
		String bairroNormalizado = bairro == null || bairro.isBlank() ? null : bairro.trim();
		Page<Ocorrencia> pagina;

		if ("prioridade".equalsIgnoreCase(sort)) {
			pagina = ocorrenciaRepository.buscarPorPrioridade(bairroNormalizado, status, pageRequest);
		} else if ("dataAbertura".equalsIgnoreCase(sort)) {
			pagina = ocorrenciaRepository.buscar(bairroNormalizado, status, pageRequest);
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ordenação inválida");
		}

		return paraPagina(pagina);
	}

	@Transactional(readOnly = true)
	public OcorrenciaResponse buscarPorId(Long id) {
		Ocorrencia ocorrencia = buscarOcorrencia(id);
		return ocorrenciaMapper.toResponse(ocorrencia, true);
	}

	@Transactional
	public OcorrenciaResponse atualizar(Long id, Long usuarioId, AtualizarOcorrenciaRequest request) {
		Ocorrencia ocorrencia = buscarOcorrencia(id);
		validarDono(ocorrencia, usuarioId);
		ocorrencia.setDescricao(request.descricao().trim());
		ocorrencia.setBairro(request.bairro().trim());
		ocorrencia.setLatitude(request.latitude());
		ocorrencia.setLongitude(request.longitude());
		ocorrencia.setDataAtualizacao(LocalDateTime.now());
		return ocorrenciaMapper.toResponse(ocorrencia, false);
	}

	@Transactional
	public void excluir(Long id, Long usuarioId) {
		Ocorrencia ocorrencia = buscarOcorrencia(id);
		validarDono(ocorrencia, usuarioId);
		ocorrenciaRepository.delete(ocorrencia);
	}

	private PaginaResponse<OcorrenciaResponse> paraPagina(Page<Ocorrencia> pagina) {
		return new PaginaResponse<>(
				pagina.getContent().stream().map(ocorrencia -> ocorrenciaMapper.toResponse(ocorrencia, false)).toList(),
				pagina.getNumber(),
				pagina.getSize(),
				pagina.getTotalElements(),
				pagina.getTotalPages());
	}

	private Usuario buscarUsuario(Long id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
	}

	private Ocorrencia buscarOcorrencia(Long id) {
		return ocorrenciaRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ocorrência não encontrada"));
	}

	private void validarDono(Ocorrencia ocorrencia, Long usuarioId) {
		if (!ocorrencia.getUsuario().getId().equals(usuarioId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não é o titular desta ocorrência");
		}
	}

	private void validarPaginacao(PageRequest pageRequest) {
		if (pageRequest.getPageNumber() < 0 || pageRequest.getPageSize() > MAX_PAGE_SIZE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parâmetros de paginação inválidos");
		}
	}

	private HistoricoStatus criarHistorico(
			Ocorrencia ocorrencia,
			Usuario usuario,
			StatusOcorrencia anterior,
			StatusOcorrencia novo,
			String comentario,
			LocalDateTime dataAlteracao) {
		HistoricoStatus historico = new HistoricoStatus();
		historico.setOcorrencia(ocorrencia);
		historico.setUsuario(usuario);
		historico.setStatusAnterior(anterior);
		historico.setStatusNovo(novo);
		historico.setComentario(comentario);
		historico.setDataAlteracao(dataAlteracao);
		return historico;
	}
}
