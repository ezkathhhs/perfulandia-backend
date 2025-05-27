package com.perfulandia.carritoservice.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "carritos") // Nombre de la tabla en la base de datos
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Carrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private Long usuarioId;
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CarritoItem> items = new ArrayList<>();
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;
    @UpdateTimestamp
    private LocalDateTime fechaActualizacion;

    // Métodos de utilidad para manejar los items
    public void addItem(CarritoItem item) {
        this.items.add(item);
        item.setCarrito(this);
    }

    public void removeItemByProductoId(Long productoId) {
        this.items.removeIf(item -> item.getProductoId().equals(productoId));
    }

    // Metodo para calcular el total del carrito
    public BigDecimal getTotalCarrito() {
        return items.stream()
                .map(CarritoItem::getSubtotal) // Usa el metodo getSubtotal de CarritoItem
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Suma todos los subtotales
    }
}
