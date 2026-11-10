package br.com.fiap.laresapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.laresapi.entity.Ocorrencia;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {
}
