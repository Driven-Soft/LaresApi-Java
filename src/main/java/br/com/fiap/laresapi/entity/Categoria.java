package br.com.fiap.laresapi.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "LARES_CATEGORIA")
public class Categoria {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "CODIGO", nullable = false, unique = true, length = 20)
	private String codigo;

	@Column(name = "NOME", nullable = false, length = 60)
	private String nome;

	@Column(name = "DESCRICAO", length = 255)
	private String descricao;

	@Column(name = "PRAZO_PADRAO_DIAS", nullable = false)
	private Integer prazoPadraoDias;

	@OneToMany(mappedBy = "categoria")
	private List<Ocorrencia> ocorrencias = new ArrayList<>();

	protected Categoria() {
	}

	public Long getId() {
		return id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Integer getPrazoPadraoDias() {
		return prazoPadraoDias;
	}

	public void setPrazoPadraoDias(Integer prazoPadraoDias) {
		this.prazoPadraoDias = prazoPadraoDias;
	}

	public List<Ocorrencia> getOcorrencias() {
		return ocorrencias;
	}
}
