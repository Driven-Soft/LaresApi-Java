package br.com.fiap.laresapi.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.fiap.laresapi.dto.UsuarioResumoResponse;
import br.com.fiap.laresapi.dto.categoria.CategoriaResponse;
import br.com.fiap.laresapi.dto.ocorrencia.HistoricoStatusResponse;
import br.com.fiap.laresapi.dto.ocorrencia.OcorrenciaResponse;
import br.com.fiap.laresapi.entity.HistoricoStatus;
import br.com.fiap.laresapi.entity.Ocorrencia;

@Component
public class OcorrenciaMapper {

	private final CategoriaMapper categoriaMapper;

	public OcorrenciaMapper(CategoriaMapper categoriaMapper) {
		this.categoriaMapper = categoriaMapper;
	}

	public OcorrenciaResponse toResponse(Ocorrencia ocorrencia, boolean incluirHistorico) {
		CategoriaResponse categoria = ocorrencia.getCategoria() == null
				? null
				: categoriaMapper.toResponse(ocorrencia.getCategoria());
		List<HistoricoStatusResponse> historico = incluirHistorico
				? ocorrencia.getHistorico().stream().map(this::toHistoricoResponse).toList()
				: null;

		return new OcorrenciaResponse(
				ocorrencia.getId(),
				ocorrencia.getDescricao(),
				ocorrencia.getBairro(),
				ocorrencia.getLatitude(),
				ocorrencia.getLongitude(),
				ocorrencia.getStatus(),
				ocorrencia.getPrioridade(),
				ocorrencia.getPrazoEstimadoDias(),
				ocorrencia.getResumoIa(),
				ocorrencia.getMensagemCidadao(),
				categoria,
				new UsuarioResumoResponse(ocorrencia.getUsuario().getId(), ocorrencia.getUsuario().getNome()),
				ocorrencia.getDataAbertura(),
				ocorrencia.getDataAtualizacao(),
				historico);
	}

	private HistoricoStatusResponse toHistoricoResponse(HistoricoStatus historico) {
		return new HistoricoStatusResponse(
				historico.getStatusAnterior(),
				historico.getStatusNovo(),
				historico.getComentario(),
				historico.getUsuario() == null ? null : historico.getUsuario().getNome(),
				historico.getDataAlteracao());
	}
}
