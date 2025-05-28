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


@Entity // Esto significa que representa una tabla en la base de datos
@Table(name = "carritos") // Nombre de la tabla en la base de datos
@Data
@NoArgsConstructor
@AllArgsConstructor

// La clase carrito es la clase que contiene los carritos
public class Carrito {

    // ID autoincrementable para cada instancia de carrito
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Almacena el ID del usuario al que pertenece este carrito. (Este ID provendría de tu usuarioservice.)
    @Column(unique = true, nullable = false)
    private Long usuarioId;

    // Contiene la lista de todos los productos individuales (como objetos CarritoItem) que el usuario ha agregado a este carrito.
    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CarritoItem> items = new ArrayList<>();

    //Registra cuándo se creó el carrito.
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    // Registra la última vez que se modificó el carrito.
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
