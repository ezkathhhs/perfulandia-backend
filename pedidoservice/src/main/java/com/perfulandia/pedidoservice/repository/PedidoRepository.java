package com.perfulandia.pedidoservice.repository;

import com.perfulandia.pedidoservice.model.Pedido;
import com.perfulandia.pedidoservice.model.EstadoPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Busca todos los pedidos realizados por un usuario específico.
    List<Pedido> findByUsuarioId(Long usuarioId);

    // Busca todos los pedidos que se encuentran en un estado específico.
    List<Pedido> findByEstado(EstadoPedido estado);

    // Busca pedidos realizados por un usuario y que además están en un estado específico.
    List<Pedido> findByUsuarioIdAndEstado(Long usuarioId, EstadoPedido estado);

    // Busca pedidos cuya fecha de creación sea anterior a una fecha dada.
    List<Pedido> findByFechaCreacionBefore(LocalDateTime fecha);

    // Busca pedidos cuya fecha de creación esté dentro de un rango de fechas.
    List<Pedido> findByFechaCreacionBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    // Busca pedidos que contengan un producto específico (por productoId en PedidoItem).
    @Query("SELECT DISTINCT p FROM Pedido p JOIN p.items i WHERE i.productoId = :productoId")
    List<Pedido> findPedidosByProductoId(@Param("productoId") Long productoId);
}