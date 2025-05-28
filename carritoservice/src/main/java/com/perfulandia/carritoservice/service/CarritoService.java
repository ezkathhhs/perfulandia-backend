package com.perfulandia.carritoservice.service;

import com.perfulandia.carritoservice.model.Carrito;
import com.perfulandia.carritoservice.model.CarritoItem;
import com.perfulandia.carritoservice.repository.CarritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class CarritoService {

    private final CarritoRepository carritoRepository;

    public CarritoService(CarritoRepository carritoRepository) {
        this.carritoRepository = carritoRepository;
    }

    //  Asegurar que un usuario siempre tenga un carrito asociado. Si ya existe uno, lo devuelve; si no, lo crea y lo devuelve.
    @Transactional
    public Carrito obtenerOCrearCarrito(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId).orElseGet(() -> {
            Carrito nuevoCarrito = new Carrito();
            nuevoCarrito.setUsuarioId(usuarioId);
            return carritoRepository.save(nuevoCarrito);
        });
    }

    // Añadir un producto (o incrementar la cantidad de uno existente) al carrito de un usuario.
    @Transactional
    public Carrito agregarItem(Long usuarioId, Long productoId, String nombreProducto, int cantidad, BigDecimal precioUnitario) {
        Carrito carrito = obtenerOCrearCarrito(usuarioId);
        Optional<CarritoItem> itemOpt = carrito.getItems().stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst();
        if (itemOpt.isPresent()) {
            CarritoItem item = itemOpt.get();
            item.setCantidad(item.getCantidad() + cantidad);
            item.setPrecioUnitario(precioUnitario); // Actualizar precio si cambia
            item.setNombreProducto(nombreProducto);
        } else {
            CarritoItem nuevoItem = new CarritoItem(null, productoId, nombreProducto, cantidad, precioUnitario, carrito);
            carrito.addItem(nuevoItem);
        }
        return carritoRepository.save(carrito);
    }

    // Modificar la cantidad de un producto específico en el carrito o eliminarlo si la cantidad se establece a cero o menos.
    @Transactional
    public Optional<Carrito> actualizarCantidadItem(Long usuarioId, Long productoId, int nuevaCantidad) {
        Optional<Carrito> carritoOpt = carritoRepository.findByUsuarioId(usuarioId);
        if (carritoOpt.isPresent()) {
            Carrito carrito = carritoOpt.get();
            Optional<CarritoItem> itemOpt = carrito.getItems().stream()
                    .filter(i -> i.getProductoId().equals(productoId))
                    .findFirst();
            if (itemOpt.isPresent()) {
                if (nuevaCantidad > 0) {
                    itemOpt.get().setCantidad(nuevaCantidad);
                } else {
                    carrito.getItems().remove(itemOpt.get());
                }
                return Optional.of(carritoRepository.save(carrito));
            }
        }
        return Optional.empty();
    }

    // Eliminar completamente un producto (sin importar su cantidad) del carrito de un usuario.
    @Transactional
    public Optional<Carrito> removerItem(Long usuarioId, Long productoId) {
        Optional<Carrito> carritoOpt = carritoRepository.findByUsuarioId(usuarioId);
        if (carritoOpt.isPresent()) {
            Carrito carrito = carritoOpt.get();
            boolean removed = carrito.getItems().removeIf(item -> item.getProductoId().equals(productoId));
            if (removed) {
                return Optional.of(carritoRepository.save(carrito));
            }
            return Optional.of(carrito);
        }
        return Optional.empty();
    }

    // Eliminar todos los ítems del carrito de un usuario, dejándolo vacío.
    @Transactional
    public Optional<Carrito> vaciarCarrito(Long usuarioId) {
        Optional<Carrito> carritoOpt = carritoRepository.findByUsuarioId(usuarioId);
        if (carritoOpt.isPresent()) {
            Carrito carrito = carritoOpt.get();
            carrito.getItems().clear();
            return Optional.of(carritoRepository.save(carrito));
        }
        return Optional.empty();
    }

    // Obtener un carrito específico por su ID único (no por el ID del usuario).
    @Transactional(readOnly = true)
    public Optional<Carrito> obtenerCarritoPorId(Long carritoId) {
        return carritoRepository.findById(carritoId);
    }

    // Eliminar un carrito completo de la base de datos por su ID.
    public void eliminarCarritoPorId(Long carritoId) {
        carritoRepository.deleteById(carritoId);
    }
}