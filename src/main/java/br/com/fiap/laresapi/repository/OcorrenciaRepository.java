package br.com.fiap.laresapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.fiap.laresapi.entity.Ocorrencia;
import br.com.fiap.laresapi.entity.StatusOcorrencia;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {

	Page<Ocorrencia> findByUsuarioId(Long usuarioId, Pageable pageable);

	@Query("""
			SELECT o FROM Ocorrencia o
			WHERE (:bairro IS NULL OR LOWER(o.bairro) = LOWER(:bairro))
			  AND (:status IS NULL OR o.status = :status)
			ORDER BY o.dataAbertura DESC
			""")
	Page<Ocorrencia> buscar(
			@Param("bairro") String bairro,
			@Param("status") StatusOcorrencia status,
			Pageable pageable);

	@Query("""
			SELECT o FROM Ocorrencia o
			WHERE (:bairro IS NULL OR LOWER(o.bairro) = LOWER(:bairro))
			  AND (:status IS NULL OR o.status = :status)
			ORDER BY CASE o.prioridade
				WHEN br.com.fiap.laresapi.entity.PrioridadeOcorrencia.ALTA THEN 0
				WHEN br.com.fiap.laresapi.entity.PrioridadeOcorrencia.MEDIA THEN 1
				WHEN br.com.fiap.laresapi.entity.PrioridadeOcorrencia.BAIXA THEN 2
				ELSE 3
			END ASC, o.dataAbertura ASC
			""")
	Page<Ocorrencia> buscarPorPrioridade(
			@Param("bairro") String bairro,
			@Param("status") StatusOcorrencia status,
			Pageable pageable);
}
