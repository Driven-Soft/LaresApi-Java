package br.com.fiap.laresapi.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "LARES_USUARIO")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "NOME", nullable = false, length = 100)
	private String nome;

	@Column(name = "EMAIL", nullable = false, unique = true, length = 150)
	private String email;

	@Column(name = "SENHA_HASH", nullable = false, length = 100)
	private String senhaHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "PAPEL", nullable = false, length = 10)
	private PapelUsuario papel;

	@Column(name = "DATA_CADASTRO", nullable = false)
	private LocalDateTime dataCadastro;

	@OneToMany(mappedBy = "usuario")
	private List<Ocorrencia> ocorrencias = new ArrayList<>();

	@OneToMany(mappedBy = "usuario")
	private List<HistoricoStatus> alteracoesHistorico = new ArrayList<>();

	protected Usuario() {
	}

	public Long getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenhaHash() {
		return senhaHash;
	}

	public void setSenhaHash(String senhaHash) {
		this.senhaHash = senhaHash;
	}

	public PapelUsuario getPapel() {
		return papel;
	}

	public void setPapel(PapelUsuario papel) {
		this.papel = papel;
	}

	public LocalDateTime getDataCadastro() {
		return dataCadastro;
	}

	public void setDataCadastro(LocalDateTime dataCadastro) {
		this.dataCadastro = dataCadastro;
	}

	public List<Ocorrencia> getOcorrencias() {
		return ocorrencias;
	}

	public List<HistoricoStatus> getAlteracoesHistorico() {
		return alteracoesHistorico;
	}
}
