package br.com.fiap.laresapi.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "LARES_OCORRENCIA")
public class Ocorrencia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "USUARIO_ID", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CATEGORIA_ID")
	private Categoria categoria;

	@Column(name = "DESCRICAO", nullable = false, length = 500)
	private String descricao;

	@Column(name = "BAIRRO", nullable = false, length = 80)
	private String bairro;

	@Column(name = "LATITUDE", precision = 9, scale = 6)
	private BigDecimal latitude;

	@Column(name = "LONGITUDE", precision = 9, scale = 6)
	private BigDecimal longitude;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", nullable = false, length = 15)
	private StatusOcorrencia status;

	@Enumerated(EnumType.STRING)
	@Column(name = "PRIORIDADE", length = 5)
	private PrioridadeOcorrencia prioridade;

	@Column(name = "PRAZO_ESTIMADO_DIAS")
	private Integer prazoEstimadoDias;

	@Column(name = "RESUMO_IA", length = 1000)
	private String resumoIa;

	@Column(name = "MENSAGEM_CIDADAO", length = 1000)
	private String mensagemCidadao;

	@Column(name = "DATA_ABERTURA", nullable = false)
	private LocalDateTime dataAbertura;

	@Column(name = "DATA_ATUALIZACAO", nullable = false)
	private LocalDateTime dataAtualizacao;

	@OneToMany(mappedBy = "ocorrencia", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("dataAlteracao ASC")
	private List<HistoricoStatus> historico = new ArrayList<>();

	protected Ocorrencia() {
	}

	public Long getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Categoria getCategoria() {
		return categoria;
	}

	public void setCategoria(Categoria categoria) {
		this.categoria = categoria;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude;
	}

	public StatusOcorrencia getStatus() {
		return status;
	}

	public void setStatus(StatusOcorrencia status) {
		this.status = status;
	}

	public PrioridadeOcorrencia getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(PrioridadeOcorrencia prioridade) {
		this.prioridade = prioridade;
	}

	public Integer getPrazoEstimadoDias() {
		return prazoEstimadoDias;
	}

	public void setPrazoEstimadoDias(Integer prazoEstimadoDias) {
		this.prazoEstimadoDias = prazoEstimadoDias;
	}

	public String getResumoIa() {
		return resumoIa;
	}

	public void setResumoIa(String resumoIa) {
		this.resumoIa = resumoIa;
	}

	public String getMensagemCidadao() {
		return mensagemCidadao;
	}

	public void setMensagemCidadao(String mensagemCidadao) {
		this.mensagemCidadao = mensagemCidadao;
	}

	public LocalDateTime getDataAbertura() {
		return dataAbertura;
	}

	public void setDataAbertura(LocalDateTime dataAbertura) {
		this.dataAbertura = dataAbertura;
	}

	public LocalDateTime getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}

	public List<HistoricoStatus> getHistorico() {
		return historico;
	}
}
