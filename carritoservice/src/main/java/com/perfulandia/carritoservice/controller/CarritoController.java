package com.perfulandia.carritoservice.controller;

import com.perfulandia.carritoservice.model.Carrito;
import com.perfulandia.carritoservice.service.CarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Optional;

@RestController // Maneja solicitudes HTTP
@RequestMapping("/api/carritos") // Establece que los endpoints comenzarán como (/api/carritos)
public class CarritoController {
    private final CarritoService carritoService;
    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }
    public record AddItemToCartRequest(Long productoId, String nombreProducto, int cantidad, BigDecimal precioUnitario) {}
    public record UpdateItemQuantityRequest(int nuevaCantidad) {}

    // Obtener el carrito de compras de un usuario específico.
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Carrito> getCarritoByUsuarioId(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(carritoService.obtenerOCrearCarrito(usuarioId));
    }

    // Agregar un producto (ítem) al carrito de un usuario específico.
    @PostMapping("/usuario/{usuarioId}/items")
    public ResponseEntity<Carrito> addItemToCart(@PathVariable Long usuarioId, @RequestBody AddItemToCartRequest request) {
        if (request.productoId == null || request.cantidad <= 0 || request.precioUnitario == null || request.nombreProducto == null || request.nombreProducto.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Carrito carrito = carritoService.agregarItem(usuarioId, request.productoId, request.nombreProducto, request.cantidad, request.precioUnitario);
        return ResponseEntity.ok(carrito);
    }

    // Actualiza la cantidad de un ítem específico que ya está en el carrito de un usuario.
    @PutMapping("/usuario/{usuarioId}/items/{productoId}")
    public ResponseEntity<Carrito> updateItemQuantity(@PathVariable Long usuarioId, @PathVariable Long productoId, @RequestBody UpdateItemQuantityRequest request) {
        if (request.nuevaCantidad < 0) return ResponseEntity.badRequest().build();
        return carritoService.actualizarCantidadItem(usuarioId, productoId, request.nuevaCantidad)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar un ítem específico del carrito de un usuario.
    @DeleteMapping("/usuario/{usuarioId}/items/{productoId}")
    public ResponseEntity<Carrito> removeItemFromCart(@PathVariable Long usuarioId, @PathVariable Long productoId) {
        return carritoService.removerItem(usuarioId, productoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar todos los ítems del carrito de un usuario específico.
    @DeleteMapping("/usuario/{usuarioId}")
    public ResponseEntity<Carrito> clearCart(@PathVariable Long usuarioId) {
        return carritoService.vaciarCarrito(usuarioId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Obtener un carrito específico por su propio ID.
    @GetMapping("/{carritoId}")
    public ResponseEntity<Carrito> getCartById(@PathVariable Long carritoId) {
        return carritoService.obtenerCarritoPorId(carritoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar un carrito completo de la base de datos por su ID.
    @DeleteMapping("/{carritoId}")
    public ResponseEntity<Void> deleteCartById(@PathVariable Long carritoId) {
        if (carritoService.obtenerCarritoPorId(carritoId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        carritoService.eliminarCarritoPorId(carritoId);
        return ResponseEntity.noContent().build();
    }
}