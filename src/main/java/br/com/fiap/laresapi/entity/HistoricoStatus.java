package br.com.fiap.laresapi.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "LARES_HISTORICO_STATUS")
public class HistoricoStatus {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "OCORRENCIA_ID", nullable = false)
	private Ocorrencia ocorrencia;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USUARIO_ID")
	private Usuario usuario;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS_ANTERIOR", length = 15)
	private StatusOcorrencia statusAnterior;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS_NOVO", nullable = false, length = 15)
	private StatusOcorrencia statusNovo;

	@Column(name = "COMENTARIO", length = 255)
	private String comentario;

	@Column(name = "DATA_ALTERACAO", nullable = false)
	private LocalDateTime dataAlteracao;

	protected HistoricoStatus() {
	}

	public Long getId() {
		return id;
	}

	public Ocorrencia getOcorrencia() {
		return ocorrencia;
	}

	public void setOcorrencia(Ocorrencia ocorrencia) {
		this.ocorrencia = ocorrencia;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public StatusOcorrencia getStatusAnterior() {
		return statusAnterior;
	}

	public void setStatusAnterior(StatusOcorrencia statusAnterior) {
		this.statusAnterior = statusAnterior;
	}

	public StatusOcorrencia getStatusNovo() {
		return statusNovo;
	}

	public void setStatusNovo(StatusOcorrencia statusNovo) {
		this.statusNovo = statusNovo;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public LocalDateTime getDataAlteracao() {
		return dataAlteracao;
	}

	public void setDataAlteracao(LocalDateTime dataAlteracao) {
		this.dataAlteracao = dataAlteracao;
	}
}
