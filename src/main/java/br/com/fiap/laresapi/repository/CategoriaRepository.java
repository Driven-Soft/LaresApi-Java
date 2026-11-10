package br.com.fiap.laresapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.fiap.laresapi.entity.Categoria;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

	Optional<Categoria> findByCodigo(String codigo);

	java.util.List<Categoria> findAllByOrderByNomeAsc();
}
