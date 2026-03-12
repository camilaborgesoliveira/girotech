package br.com.girotech.motor.infrastructure.repository;

import br.com.girotech.motor.infrastructure.entity.Categoria;
import br.com.girotech.motor.infrastructure.entity.Produto;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepositorio extends JpaRepository<Produto, Long> {
    List<Produto> findByCategoria(Categoria categoria);

    List<Produto> findByCategoriaIn(List<Categoria> categorias);

    @Query("SELECT p FROM Produto p WHERE p.preco BETWEEN :min AND :max ORDER BY p.preco ASC")
    List<Produto> buscarPorFaixaDePreco(@Param("min") BigDecimal precoMinimo, @Param("max") BigDecimal precoMaximo);
}

