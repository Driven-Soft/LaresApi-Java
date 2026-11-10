package br.com.fiap.laresapi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.laresapi.entity.HistoricoStatus;

public interface HistoricoStatusRepository extends JpaRepository<HistoricoStatus, Long> {

	List<HistoricoStatus> findByOcorrenciaIdOrderByDataAlteracaoAsc(Long ocorrenciaId);
}
