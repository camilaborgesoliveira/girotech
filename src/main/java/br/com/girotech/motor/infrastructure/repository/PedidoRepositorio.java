package br.com.girotech.motor.infrastructure.repository;

import br.com.girotech.motor.infrastructure.entity.Pedido;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepositorio extends JpaRepository<Pedido, Long> {
    @Query("SELECT p FROM Pedido p\nJOIN FETCH p.produto prod\nJOIN FETCH prod.categoria cat\nWHERE p.cliente.id = :clienteId\nORDER BY p.dataCompra DESC\n")
    List<Pedido> buscarPedidosComProdutoPorCliente(@Param("clienteId") Long clienteId);

    long countByClienteId(Long clienteId);
}