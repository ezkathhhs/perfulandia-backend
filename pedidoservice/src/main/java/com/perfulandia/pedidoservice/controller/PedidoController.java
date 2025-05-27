package com.perfulandia.pedidoservice.controller;

import com.perfulandia.pedidoservice.model.Pedido;
import com.perfulandia.pedidoservice.model.EstadoPedido;
import com.perfulandia.pedidoservice.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController // Maneja solicitudes HTTP
@RequestMapping("/api/pedidos") // Establece que los endpoints comenzarán como (/api/pedidos)
public class PedidoController {
    private final PedidoService pedidoService;
    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }
    public record CreatePedidoItemRequest(Long productoId, int cantidad) {}
    public record CreatePedidoRequest(Long usuarioId, String direccionEnvio, String ciudadEnvio, String codigoPostalEnvio, List<CreatePedidoItemRequest> items) {}
    public record UpdatePedidoStatusRequest(EstadoPedido nuevoEstado) {}

    // Crear un nuevo pedido en el sistema.
    @PostMapping
    public ResponseEntity<?> createPedido(@RequestBody CreatePedidoRequest request) {
        if (request.usuarioId == null || request.items == null || request.items.isEmpty() || request.direccionEnvio == null || request.direccionEnvio.isBlank()) {
            return ResponseEntity.badRequest().body("Datos de solicitud incompletos o inválidos.");
        }
        try {
            List<PedidoService.CreatePedidoItemInfo> itemsInfo = request.items.stream()
                    .map(itemDto -> new PedidoService.CreatePedidoItemInfo(itemDto.productoId, itemDto.cantidad))
                    .toList();
            Pedido pedido = pedidoService.crearPedido(request.usuarioId, request.direccionEnvio, request.ciudadEnvio, request.codigoPostalEnvio, itemsInfo);
            return new ResponseEntity<>(pedido, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Obtener los detalles de un pedido específico por su ID.
    @GetMapping("/{pedidoId}")
    public ResponseEntity<Pedido> getPedidoById(@PathVariable Long pedidoId) {
        return pedidoService.obtenerPedidoPorId(pedidoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener todos los pedidos realizados por un usuario específico.
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Pedido>> getPedidosByUsuarioId(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(pedidoService.obtenerPedidosPorUsuario(usuarioId));
    }

    // Actualizar el estado de un pedido existente
    @PutMapping("/{pedidoId}/estado")
    public ResponseEntity<Pedido> updatePedidoStatus(@PathVariable Long pedidoId, @RequestBody UpdatePedidoStatusRequest request) {
        if (request.nuevoEstado == null) return ResponseEntity.badRequest().build();
        return pedidoService.actualizarEstadoPedido(pedidoId, request.nuevoEstado)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener una lista de todos los pedidos registrados.
    @GetMapping
    public ResponseEntity<List<Pedido>> getAllPedidos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    // Eliminar un pedido específico del sistema por su ID.
    @DeleteMapping("/{pedidoId}")
    public ResponseEntity<Void> deletePedido(@PathVariable Long pedidoId) {
        if (pedidoService.eliminarPedidoPorId(pedidoId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}