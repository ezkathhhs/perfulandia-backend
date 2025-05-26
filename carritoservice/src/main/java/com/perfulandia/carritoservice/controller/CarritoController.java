package com.perfulandia.carritoservice.controller;

import com.perfulandia.carritoservice.model.Carrito;
import com.perfulandia.carritoservice.service.CarritoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/carritos")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Carrito> obtenerCarritoPorUsuario(@PathVariable Long usuarioId) {
        Carrito carrito = carritoService.obtenerOCrearCarritoPorUsuarioId(usuarioId);
        return ResponseEntity.ok(carrito);
    }

    public static class AddItemRequest {
        private Long productoId;
        private String nombreProducto;
        private int cantidad;
        private BigDecimal precioUnitario;

        public Long getProductoId() { return productoId; }
        public void setProductoId(Long productoId) { this.productoId = productoId; }
        public String getNombreProducto() { return nombreProducto; }
        public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
        public int getCantidad() { return cantidad; }
        public void setCantidad(int cantidad) { this.cantidad = cantidad; }
        public BigDecimal getPrecioUnitario() { return precioUnitario; }
        public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    }

    @PostMapping("/usuario/{usuarioId}/items")
    public ResponseEntity<Carrito> agregarItem(
            @PathVariable Long usuarioId,
            @RequestBody AddItemRequest request) {
        if (request.getProductoId() == null || request.getCantidad() <= 0 || request.getPrecioUnitario() == null || request.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().build();
        }
        Carrito carritoActualizado = carritoService.agregarItemAlCarrito(
                usuarioId,
                request.getProductoId(),
                request.getNombreProducto(),
                request.getCantidad(),
                request.getPrecioUnitario()
        );
        return ResponseEntity.ok(carritoActualizado);
    }

    @DeleteMapping("/usuario/{usuarioId}/items/{productoId}")
    public ResponseEntity<Carrito> eliminarItem(
            @PathVariable Long usuarioId,
            @PathVariable Long productoId) {
        Optional<Carrito> carritoActualizadoOpt = carritoService.eliminarItemDelCarrito(usuarioId, productoId);
        return carritoActualizadoOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/usuario/{usuarioId}/vaciar")
    public ResponseEntity<Carrito> vaciarCarrito(@PathVariable Long usuarioId) {
        Optional<Carrito> carritoVaciadoOpt = carritoService.vaciarCarrito(usuarioId);
        return carritoVaciadoOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Carrito> listarTodosLosCarritos() {
        return carritoService.listarTodosLosCarritos();
    }

    @GetMapping("/{idCarrito}")
    public ResponseEntity<Carrito> buscarCarritoPorId(@PathVariable Long idCarrito) {
        Carrito carrito = carritoService.buscarCarritoPorId(idCarrito);
        if (carrito != null) {
            return ResponseEntity.ok(carrito);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{idCarrito}")
    public ResponseEntity<Void> eliminarCarritoPorId(@PathVariable Long idCarrito) {
        if (carritoService.buscarCarritoPorId(idCarrito) == null) {
            return ResponseEntity.notFound().build();
        }
        carritoService.eliminarCarritoPorId(idCarrito);
        return ResponseEntity.noContent().build();
    }
}