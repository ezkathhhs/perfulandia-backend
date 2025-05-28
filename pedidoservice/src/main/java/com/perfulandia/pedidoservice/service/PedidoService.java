package com.perfulandia.pedidoservice.service;

import com.perfulandia.pedidoservice.model.Pedido;
import com.perfulandia.pedidoservice.model.PedidoItem;
import com.perfulandia.pedidoservice.model.EstadoPedido;
import com.perfulandia.pedidoservice.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final RestTemplate restTemplate;

    @Value("${productoservice.url}")
    private String productoServiceUrl;

    // DTO para la respuesta de producto-service
    public record ProductoResponseDto(Long id, String nombre, BigDecimal precio, int stock) {}
    // DTO para la información de un item en la creación del pedido.
    public record CreatePedidoItemInfo(Long productoId, int cantidad) {}
    public PedidoService(PedidoRepository pedidoRepository, RestTemplate restTemplate) {
        this.pedidoRepository = pedidoRepository;
        this.restTemplate = restTemplate;
    }

    // Crear un nuevo pedido, obteniendo información actualizada de los productos desde productoservice, calculando el total y persistirlo en la base de datos.
    @Transactional
    public Pedido crearPedido(Long usuarioId, String direccionEnvio, String ciudadEnvio, String codigoPostalEnvio, List<CreatePedidoItemInfo> itemsInfo) {
        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setUsuarioId(usuarioId);
        nuevoPedido.setDireccionEnvio(direccionEnvio);
        nuevoPedido.setCiudadEnvio(ciudadEnvio);
        nuevoPedido.setCodigoPostalEnvio(codigoPostalEnvio);
        for (CreatePedidoItemInfo itemInfo : itemsInfo) {
            ProductoResponseDto productoActual;
            try {
                String url = productoServiceUrl + "/" + itemInfo.productoId();
                productoActual = restTemplate.getForObject(url, ProductoResponseDto.class);
            } catch (HttpClientErrorException.NotFound ex) {
                throw new RuntimeException("Producto con ID " + itemInfo.productoId() + " no encontrado en producto-service.");
            } catch (Exception ex) {
                throw new RuntimeException("Error al contactar producto-service para producto ID " + itemInfo.productoId() + ": " + ex.getMessage());
            }
            if (productoActual == null) {
                throw new RuntimeException("Producto con ID " + itemInfo.productoId() + " devolvió null desde producto-service.");
            }
            PedidoItem pi = new PedidoItem();
            pi.setProductoId(itemInfo.productoId());
            pi.setNombreProducto(productoActual.nombre()); // Nombre actualizado
            pi.setCantidad(itemInfo.cantidad());
            pi.setPrecioUnitario(productoActual.precio()); // Precio actualizado
            nuevoPedido.addItem(pi);
        }
        nuevoPedido.calcularTotalPedido();
        // Estado PENDIENTE y fechas se manejan por @PrePersist y @CreationTimestamp
        return pedidoRepository.save(nuevoPedido);
    }

    // Recuperar un pedido específico de la base de datos por su ID.
    @Transactional(readOnly = true)
    public Optional<Pedido> obtenerPedidoPorId(Long pedidoId) {
        return pedidoRepository.findById(pedidoId);
    }

    // Listar todos los pedidos realizados por un usuario en particular.
    @Transactional(readOnly = true)
    public List<Pedido> obtenerPedidosPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    // Modificar el estado de un pedido existente
    @Transactional
    public Optional<Pedido> actualizarEstadoPedido(Long pedidoId, EstadoPedido nuevoEstado) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(pedidoId);
        if (pedidoOpt.isPresent()) {
            Pedido pedido = pedidoOpt.get();
            pedido.setEstado(nuevoEstado);
            return Optional.of(pedidoRepository.save(pedido));
        }
        return Optional.empty();
    }

    // Obtener una lista de todos los pedidos en el sistema
    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    // Eliminar un pedido específico de la base de datos por su ID.
    @Transactional
    public boolean eliminarPedidoPorId(Long pedidoId) {
        if (pedidoRepository.existsById(pedidoId)) {
            pedidoRepository.deleteById(pedidoId);
            return true;
        }
        return false;
    }
}