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
import br.com.fiap.laresapi.dto.ocorrencia.AtualizarStatusRequest;
import br.com.fiap.laresapi.dto.ocorrencia.OcorrenciaRequest;
import br.com.fiap.laresapi.dto.ocorrencia.OcorrenciaResponse;
import br.com.fiap.laresapi.entity.Categoria;
import br.com.fiap.laresapi.entity.HistoricoStatus;
import br.com.fiap.laresapi.entity.Ocorrencia;
import br.com.fiap.laresapi.entity.StatusOcorrencia;
import br.com.fiap.laresapi.entity.Usuario;
import br.com.fiap.laresapi.mapper.OcorrenciaMapper;
import br.com.fiap.laresapi.messaging.OcorrenciaCriadaEvent;
import br.com.fiap.laresapi.repository.CategoriaRepository;
import br.com.fiap.laresapi.repository.OcorrenciaRepository;
import br.com.fiap.laresapi.repository.UsuarioRepository;
import org.springframework.context.ApplicationEventPublisher;

@Service
public class OcorrenciaService {

	private static final int MAX_PAGE_SIZE = 100;

	private final OcorrenciaRepository ocorrenciaRepository;
	private final UsuarioRepository usuarioRepository;
	private final CategoriaRepository categoriaRepository;
	private final OcorrenciaMapper ocorrenciaMapper;
	private final ApplicationEventPublisher eventPublisher;

	public OcorrenciaService(
			OcorrenciaRepository ocorrenciaRepository,
			UsuarioRepository usuarioRepository,
			CategoriaRepository categoriaRepository,
			OcorrenciaMapper ocorrenciaMapper,
			ApplicationEventPublisher eventPublisher) {
		this.ocorrenciaRepository = ocorrenciaRepository;
		this.usuarioRepository = usuarioRepository;
		this.categoriaRepository = categoriaRepository;
		this.ocorrenciaMapper = ocorrenciaMapper;
		this.eventPublisher = eventPublisher;
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
		eventPublisher.publishEvent(new OcorrenciaCriadaEvent(salva.getId()));
		return ocorrenciaMapper.toResponse(salva, false);
	}

	@Transactional(readOnly = true)
	public PaginaResponse<OcorrenciaResponse> listarMinhas(Long usuarioId, int page, int size) {
		PageRequest pageRequest = criarPageRequest(page, size);
		Page<Ocorrencia> pagina = ocorrenciaRepository.findByUsuarioId(usuarioId, pageRequest);
		return paraPagina(pagina);
	}

	@Transactional(readOnly = true)
	public PaginaResponse<OcorrenciaResponse> buscar(
			String bairro,
			StatusOcorrencia status,
			String sort,
			int page,
			int size) {
		PageRequest pageRequest = criarPageRequest(page, size);
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
		validarEditavel(ocorrencia);
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
		validarEditavel(ocorrencia);
		ocorrenciaRepository.delete(ocorrencia);
	}

	@Transactional
	public OcorrenciaResponse atualizarStatus(Long id, Long usuarioId, AtualizarStatusRequest request) {
		Ocorrencia ocorrencia = buscarOcorrencia(id);
		Usuario gestor = buscarUsuario(usuarioId);
		StatusOcorrencia anterior = ocorrencia.getStatus();
		StatusOcorrencia novo = request.status();

		if (!transicaoPermitida(anterior, novo)) {
			throw new ResponseStatusException(
					HttpStatus.CONFLICT,
					"Transição de status não permitida: " + anterior + " para " + novo);
		}

		if (request.categoriaId() != null) {
			Categoria categoria = categoriaRepository.findById(request.categoriaId())
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada"));
			ocorrencia.setCategoria(categoria);
		}

		LocalDateTime agora = LocalDateTime.now();
		ocorrencia.setStatus(novo);
		ocorrencia.setDataAtualizacao(agora);
		ocorrencia.adicionarHistorico(criarHistorico(
				ocorrencia, gestor, anterior, novo, normalizarComentario(request.comentario()), agora));
		return ocorrenciaMapper.toResponse(ocorrencia, false);
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

	private void validarEditavel(Ocorrencia ocorrencia) {
		if (ocorrencia.getStatus() != StatusOcorrencia.RECEBIDA
				&& ocorrencia.getStatus() != StatusOcorrencia.TRIADA) {
			throw new ResponseStatusException(
					HttpStatus.CONFLICT,
					"Ocorrências só podem ser editadas ou excluídas enquanto RECEBIDAS ou TRIADAS");
		}
	}

	private boolean transicaoPermitida(StatusOcorrencia anterior, StatusOcorrencia novo) {
		return (anterior == StatusOcorrencia.RECEBIDA && novo == StatusOcorrencia.EM_ANDAMENTO)
				|| (anterior == StatusOcorrencia.TRIADA && novo == StatusOcorrencia.EM_ANDAMENTO)
				|| (anterior == StatusOcorrencia.EM_ANDAMENTO && novo == StatusOcorrencia.RESOLVIDA);
	}

	private String normalizarComentario(String comentario) {
		return comentario == null || comentario.isBlank() ? null : comentario.trim();
	}

	private PageRequest criarPageRequest(int page, int size) {
		if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parâmetros de paginação inválidos");
		}
		return PageRequest.of(page, size);
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
