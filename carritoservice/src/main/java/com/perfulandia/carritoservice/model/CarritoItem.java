package com.perfulandia.carritoservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal; // Para manejar precios con precisión
import lombok.Builder;

@Entity
@Table(name = "carrito_items") // Nombre de la tabla en la base de datos
@Data // Genera getters, setters, toString, equals, hashCode
@NoArgsConstructor // Genera un constructor sin argumentos
@AllArgsConstructor // Genera un constructor con todos los argumentos
@Builder

public class CarritoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Autoincremental por la BD
    private Long id;
    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal precioUnitario;

    // Un CarritoItem pertenece a un solo Carrito.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    // Metodo para calcular el subtotal de este item
    public BigDecimal getSubtotal() {
        if (precioUnitario == null || cantidad == null) {
            return BigDecimal.ZERO;
        }
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}
