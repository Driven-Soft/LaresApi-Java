package br.com.fiap.laresapi.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.com.fiap.laresapi.dto.ai.ConteudoOcorrenciaIa;
import br.com.fiap.laresapi.dto.triagem.TriagemRequest;
import br.com.fiap.laresapi.dto.triagem.TriagemResultado;
import br.com.fiap.laresapi.entity.Categoria;
import br.com.fiap.laresapi.entity.HistoricoStatus;
import br.com.fiap.laresapi.entity.Ocorrencia;
import br.com.fiap.laresapi.entity.StatusOcorrencia;
import br.com.fiap.laresapi.repository.CategoriaRepository;
import br.com.fiap.laresapi.repository.OcorrenciaRepository;

@Service
public class TriagemService {

	private final OcorrenciaRepository ocorrenciaRepository;
	private final CategoriaRepository categoriaRepository;

	public TriagemService(OcorrenciaRepository ocorrenciaRepository, CategoriaRepository categoriaRepository) {
		this.ocorrenciaRepository = ocorrenciaRepository;
		this.categoriaRepository = categoriaRepository;
	}

	@Transactional(readOnly = true)
	public Optional<TriagemRequest> obterDados(Long ocorrenciaId) {
		return ocorrenciaRepository.findById(ocorrenciaId)
				.filter(ocorrencia -> ocorrencia.getStatus() == StatusOcorrencia.RECEBIDA)
				.map(ocorrencia -> new TriagemRequest(ocorrencia.getDescricao(), ocorrencia.getBairro()));
	}

	@Transactional
	public void aplicarResultado(Long ocorrenciaId, TriagemResultado resultado) {
		aplicarResultado(ocorrenciaId, resultado, null);
	}

	@Transactional
	public void aplicarResultado(Long ocorrenciaId, TriagemResultado resultado, ConteudoOcorrenciaIa conteudo) {
		Ocorrencia ocorrencia = ocorrenciaRepository.findById(ocorrenciaId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ocorrência não encontrada"));
		if (ocorrencia.getStatus() != StatusOcorrencia.RECEBIDA) {
			return;
		}

		Categoria categoria = categoriaRepository.findByCodigo(resultado.categoriaCodigo())
				.orElseThrow(() -> new IllegalArgumentException(
						"Categoria desconhecida retornada pelo serviço de triagem: " + resultado.categoriaCodigo()));
		LocalDateTime agora = LocalDateTime.now();
		HistoricoStatus historico = new HistoricoStatus();
		historico.setOcorrencia(ocorrencia);
		historico.setUsuario(null);
		historico.setStatusAnterior(StatusOcorrencia.RECEBIDA);
		historico.setStatusNovo(StatusOcorrencia.TRIADA);
		historico.setDataAlteracao(agora);

		ocorrencia.setCategoria(categoria);
		ocorrencia.setPrioridade(resultado.prioridade());
		ocorrencia.setPrazoEstimadoDias(resultado.prazoEstimadoDias());
		if (conteudo != null) {
			ocorrencia.setResumoIa(conteudo.resumoIa());
			ocorrencia.setMensagemCidadao(conteudo.mensagemCidadao());
		}
		ocorrencia.setStatus(StatusOcorrencia.TRIADA);
		ocorrencia.setDataAtualizacao(agora);
		ocorrencia.adicionarHistorico(historico);
		ocorrenciaRepository.save(ocorrencia);
	}
}