package com.cordillera.ventas.Model;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "ventas_consolidadas")
@Data
public class VentaModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación Many-to-One: Muchas ventas pertenecen a una sucursal
    private Long productoId; // Solo guardamos la referencia numéricaaaa
    private Long sucursalId;
    private String origen; // Ej: "FISICO" o "WEB"
    private Integer cantidad;
    private Double montoTotal;
    private LocalDateTime fechaVenta;
}
