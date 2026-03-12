package br.com.girotech.motor.infrastructure.repository;

import br.com.girotech.motor.infrastructure.entity.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNome(String nome);

    List<Categoria> findByTendenciaTrue();
}
