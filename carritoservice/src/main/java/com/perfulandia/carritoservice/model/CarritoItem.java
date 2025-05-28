package com.perfulandia.carritoservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal; // Para manejar precios con precisión
import lombok.Builder;

@Entity // Esto significa que representa una tabla en la base de datos
@Table(name = "carrito_items") // Nombre de la tabla en la base de datos
@Data // Genera getters, setters, toString, equals, hashCode
@NoArgsConstructor // Genera un constructor sin argumentos
@AllArgsConstructor // Genera un constructor con todos los argumentos
@Builder

public class CarritoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental por la BD

    // Identificador único para cada ítem individual dentro de un carrito.
    private Long id;

    // Almacena el ID del producto al que el ítem del carrito hace referencia. (Este ID provendría de tu productoservice)
    private Long productoId;

    // Almacena el nombre del producto
    private String nombreProducto;

    // Representa cuántas unidades de este productoId específico se han agregado al carrito.
    private Integer cantidad;

    // Almacena el precio de una sola unidad del producto en el momento en que se agregó al carrito.
    private BigDecimal precioUnitario;

    // Un CarritoItem pertenece a un solo Carrito.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)

    // Establece la relación con la entidad Carrito a la que pertenece este ítem.
    private Carrito carrito;

    // Metodo para calcular el subtotal de este item
    public BigDecimal getSubtotal() {
        if (precioUnitario == null || cantidad == null) {
            return BigDecimal.ZERO;
        }
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
