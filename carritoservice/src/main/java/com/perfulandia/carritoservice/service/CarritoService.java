package com.perfulandia.carritoservice.service;

import com.perfulandia.carritoservice.model.Carrito;
import com.perfulandia.carritoservice.model.CarritoItem;
import com.perfulandia.carritoservice.repository.CarritoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;

    // Constructor para la inyección de dependencias de CarritoRepository
    public CarritoService(CarritoRepository carritoRepository) {
        this.carritoRepository = carritoRepository;
    }

    public List<Carrito> listarTodosLosCarritos() {
        return carritoRepository.findAll();
    }

    @Transactional(readOnly = true) // Buena práctica para operaciones de solo lectura
    public Optional<Carrito> obtenerCarritoPorUsuarioId(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public Carrito obtenerOCrearCarritoPorUsuarioId(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Carrito nuevoCarrito = new Carrito();
                    nuevoCarrito.setUsuarioId(usuarioId);
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    @Transactional
    public Carrito agregarItemAlCarrito(Long usuarioId, Long productoId, String nombreProducto, int cantidad, BigDecimal precioUnitario) {
        Carrito carrito = obtenerOCrearCarritoPorUsuarioId(usuarioId);

        Optional<CarritoItem> itemExistente = carrito.getItems().stream()
                .filter(item -> item.getProductoId().equals(productoId))
                .findFirst();

        if (itemExistente.isPresent()) {
            // El producto ya está en el carrito, actualizamos la cantidad
            CarritoItem item = itemExistente.get();
            item.setCantidad(item.getCantidad() + cantidad);
        } else {
            // El producto no está en el carrito, creamos un nuevo CarritoItem
            CarritoItem nuevoItem = new CarritoItem();
            nuevoItem.setProductoId(productoId);
            nuevoItem.setNombreProducto(nombreProducto);
            nuevoItem.setCantidad(cantidad);
            nuevoItem.setPrecioUnitario(precioUnitario);
            nuevoItem.setCarrito(carrito);
            carrito.addItem(nuevoItem);
        }
        return carritoRepository.save(carrito); // Guardamos el carrito con el nuevo item o la cantidad actualizada
    }

    @Transactional
    public Optional<Carrito> eliminarItemDelCarrito(Long usuarioId, Long productoId) {
        Optional<Carrito> carritoOpt = carritoRepository.findByUsuarioId(usuarioId);
        if (carritoOpt.isPresent()) {
            Carrito carrito = carritoOpt.get();
            boolean removed = carrito.getItems().removeIf(item -> item.getProductoId().equals(productoId));
            if (removed) {
                return Optional.of(carritoRepository.save(carrito));
            }
            return Optional.of(carrito); // No se eliminó nada, pero el carrito existe
        }
        return Optional.empty(); // Carrito no encontrado para el usuario
    }

    @Transactional
    public Optional<Carrito> vaciarCarrito(Long usuarioId) {
        Optional<Carrito> carritoOpt = carritoRepository.findByUsuarioId(usuarioId);
        if (carritoOpt.isPresent()) {
            Carrito carrito = carritoOpt.get();
            carrito.getItems().clear(); // Elimina todos los items
            return Optional.of(carritoRepository.save(carrito));
        }
        return Optional.empty(); // Carrito no encontrado para el usuario
    }

    @Transactional
    public void eliminarCarritoPorId(Long idCarrito) {
        carritoRepository.deleteById(idCarrito);
    }

    public Carrito buscarCarritoPorId(Long idCarrito) {
        return carritoRepository.findById(idCarrito).orElse(null);
    }
}